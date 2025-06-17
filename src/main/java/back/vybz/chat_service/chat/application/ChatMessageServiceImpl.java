package back.vybz.chat_service.chat.application;

import back.vybz.chat_service.chat.domain.ChatMessage;
import back.vybz.chat_service.chat.domain.MessageType;
import back.vybz.chat_service.chat.dto.request.RequestLeaveChatRoomDto;
import back.vybz.chat_service.chat.dto.request.RequestSendMessageDto;
import back.vybz.chat_service.chat.dto.response.ResponseChatMessageDto;
import back.vybz.chat_service.chat.infrastructure.ChatMessageReactiveRepository;
import back.vybz.chat_service.common.util.ChatSinkManager;
import back.vybz.chat_service.common.util.CursorPageUtil;
import back.vybz.chat_service.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final RedisUtil redisUtil;
    private final ChatRoomService chatRoomService;
    private final ChatSinkManager chatSinkManager;
    private final ChatMessageReactiveRepository chatMessageReactiveRepository;


    /**
     * 메시지 전송 (Reactive 저장 + 마지막 메시지 갱신)
     *
     * @param requestSendMessageDto
     */
    @Override
    public Mono<Void> sendMessage(RequestSendMessageDto requestSendMessageDto) {
        // 상대방이 채팅 방에 들어왔는지 확인
        boolean receiverOnline = redisUtil.isParticipantOnline(requestSendMessageDto.getChatRoomId(), requestSendMessageDto.getReceiverUuid());
        // 메시지 객체 생성 및 읽음 상태 설정
        ChatMessage chatMessage = requestSendMessageDto.toDocument();
        chatMessage.markReadIfReceiverOnline(receiverOnline);
        // 상대방이 채팅방을 나갔다면 재참여 처리
        chatRoomService.rejoinIfHidden(requestSendMessageDto.getChatRoomId(), requestSendMessageDto.getReceiverUuid());
        return chatMessageReactiveRepository.save(chatMessage)
                .doOnSuccess(savedMessage -> {
                    // 채팅방 마지막 메시지 갱신
                    chatRoomService.updateLastMessage(requestSendMessageDto.getChatRoomId(), savedMessage);
                    if (!receiverOnline) {
                        chatRoomService.increaseUnreadCount(requestSendMessageDto.getChatRoomId(), requestSendMessageDto.getSenderUuid());
                    }
                })
                .then();
    }

    /**
     * 채팅방 ID로 메시지 스트림 구독
     *
     * @param chatRoomId
     */
    @Override
    public Flux<ResponseChatMessageDto> subscribeChatMessageByChatRoomId(String chatRoomId, String participantUuid) {
        log.info("✅ sinkMap 진입: chatRoomId={}", chatRoomId);
        // 참가자 등록
        registerParticipant(chatRoomId, participantUuid);
        // 메시지 수신용 Flux 생성(sink 기반)
        Flux<ResponseChatMessageDto> messageFlux = chatSinkManager.getOrCreateSink(chatRoomId).asFlux();
        // ping 전송용 Flux 생성
        Flux<ResponseChatMessageDto> pingFlux = makePingFlux(chatRoomId);
        // SSE 스트림 반환
        return Flux.merge(messageFlux, pingFlux)
                .doOnSubscribe(sub -> log.info("👀 SSE 구독 시작: chatRoomId={}, participantUuid={}", chatRoomId, participantUuid))
                .doFinally(signalType -> {
                    log.info("❌ SSE 종료 감지: {}, chatRoomId={}, participantUuid={}", signalType, chatRoomId, participantUuid);
                    unregisterParticipant(chatRoomId, participantUuid);
                });
    }

    /**
     * 채팅방 ID로 이전 메시지 조회
     *
     * @param chatRoomId
     */
    @Override
    public Mono<CursorPageUtil<ResponseChatMessageDto, Instant>> getPreviousChatMessageByChatRoomId(String chatRoomId, String participantUuid, Instant sentAt, Integer pageSize) {
        // 읽지 않은 메시지 읽음으로 표시, 실시간 emit
        return markUnreadMessagesAsRead(chatRoomId, participantUuid)
                // 읽지 않은 메시지 수 초기화
                .then(resetUnreadCount(chatRoomId, participantUuid))
                // 채팅방의 모든 메시지 조회
                .then(fetchMessagesConsideringLeaveWithCursor(chatRoomId, participantUuid, sentAt, pageSize))
                .map(messages -> {
                    boolean hasNext = messages.size() > pageSize;
                    if (hasNext) {
                        messages = messages.subList(0, pageSize);
                    }
                    Instant nextCursor = hasNext ? messages.get(messages.size() - 1).getSentAt() : null;
                    return CursorPageUtil.<ResponseChatMessageDto, Instant>builder()
                            .content(messages)
                            .nextCursor(nextCursor)
                            .hasNext(hasNext)
                            .pageSize(pageSize)
                            .build();
                });
    }

    /**
     * 싱크로 메시지 발행
     * @param chatRoomId
     * @param responseChatMessageDto
     */
    @Override
    public void emitToSink(String chatRoomId, ResponseChatMessageDto responseChatMessageDto) {
        chatSinkManager.emitToSink(chatRoomId, responseChatMessageDto);
    }

    /**
     * 채팅방 참여자 등록
     * @param chatRoomId
     * @param participantUuid
     */
    @Override
    public void registerParticipant(String chatRoomId, String participantUuid) {
        redisUtil.addParticipantToChatRoom(chatRoomId, participantUuid);
        chatSinkManager.addParticipant(chatRoomId, participantUuid);
    }

    /**
     * 채팅방 참여자 등록 해제
     * @param chatRoomId
     * @param participantUuid
     */
    @Override
    public void unregisterParticipant(String chatRoomId, String participantUuid) {
        redisUtil.removeParticipantFromChatRoom(chatRoomId, participantUuid);
        chatSinkManager.removeParticipant(chatRoomId, participantUuid);
    }

    /**
     * 핑 메시지 발행
     * @param chatRoomId
     */
    @Override
    public Flux<ResponseChatMessageDto> makePingFlux(String chatRoomId) {
        return Flux.interval(Duration.ofSeconds(5))
                .map(tick -> ResponseChatMessageDto.ping(chatRoomId));
    }

    /**
     * 읽지 않은 메시지를 읽음으로 표시
     * @param chatRoomId
     * @param participantUuid
     */
    @Override
    public Mono<Void> markUnreadMessagesAsRead(String chatRoomId, String participantUuid) {
        return chatMessageReactiveRepository.findUnreadMessagesByChatRoomIdAndNotSender(chatRoomId, participantUuid)
                .flatMap(message -> {
                    message.markAsRead();
                    return chatMessageReactiveRepository.save(message)
                            .doOnSuccess(updated -> {
                                emitToSink(chatRoomId, ResponseChatMessageDto.from(updated));
                            });
                }).then();
    }

    /**
     * 읽지않은 메시지 수 초기화
     * @param chatRoomId
     * @param participantUuid
     */
    @Override
    public Mono<Void> resetUnreadCount(String chatRoomId, String participantUuid) {
        return Mono.fromRunnable(() -> chatRoomService.resetUnreadCount(chatRoomId, participantUuid));
    }

    /**
     * 채팅방의 메시지 조회, 참여자 퇴장 고려(커서 기반)
     * @param chatRoomId
     * @param participantUuid
     * @param sentAt
     * @param pageSize
     */
    @Override
    public Mono<List<ResponseChatMessageDto>> fetchMessagesConsideringLeaveWithCursor(String chatRoomId, String participantUuid, Instant sentAt, Integer pageSize) {
        return chatMessageReactiveRepository
                .findFirstByChatRoomIdAndSenderUuidAndMessageTypeOrderBySentAtDesc(chatRoomId, participantUuid, MessageType.LEFT)
                .flatMap(leftMessage ->
                    chatMessageReactiveRepository
                            .findByChatRoomIdWithCursorAndAfterLeft(chatRoomId, sentAt, pageSize)
                            .map(ResponseChatMessageDto::from)
                            .collectList()
                )
                .switchIfEmpty(
                    chatMessageReactiveRepository
                            .findByChatRoomIdWithCursor(chatRoomId, sentAt, pageSize)
                            .map(ResponseChatMessageDto::from)
                            .collectList()
                );
    }

    /**
     * 채팅방 나가기 메시지 발행
     * @param requestLeaveChatRoomDto
     */
    @Override
    public Mono<Void> leaveChatRoomMessage(RequestLeaveChatRoomDto requestLeaveChatRoomDto) {
        return chatMessageReactiveRepository.save(requestLeaveChatRoomDto.toDocument()).then();
    }

}
