package back.vybz.chat_service.chat.application;

import back.vybz.chat_service.chat.domain.ChatMessage;
import back.vybz.chat_service.chat.dto.request.RequestSendMessageDto;
import back.vybz.chat_service.chat.dto.response.ResponseChatMessageDto;
import back.vybz.chat_service.chat.infrastructure.ChatMessageReactiveRepository;
import back.vybz.chat_service.common.util.ChatSinkManager;
import back.vybz.chat_service.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageReactiveRepository chatMessageReactiveRepository;
    private final ChatRoomService chatRoomService;
    private final Map<String, Sinks.Many<ResponseChatMessageDto>> sinkMap = new ConcurrentHashMap<>();
    private final RedisUtil redisUtil;
    private final ChatSinkManager chatSinkManager;


    /**
     * 메시지 전송 (Reactive 저장 + 마지막 메시지 갱신)
     *
     * @param requestSendMessageDto
     */
    @Override
    public Mono<Void> sendMessage(RequestSendMessageDto requestSendMessageDto) {
        boolean receiverOnline = redisUtil.isParticipantOnline(requestSendMessageDto.getChatRoomId(), requestSendMessageDto.getReceiverUuid());
        ChatMessage chatMessage = requestSendMessageDto.toDocument();
        chatMessage.markReadIfReceiverOnline(receiverOnline);
        return chatMessageReactiveRepository.save(chatMessage)
                .doOnSuccess(savedMessage -> {
                    chatRoomService.updateLastMessage(requestSendMessageDto.getChatRoomId(), savedMessage);
                    chatRoomService.increaseUnreadCount(requestSendMessageDto.getChatRoomId(), requestSendMessageDto.getSenderUuid());
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

        redisUtil.addParticipantToChatRoom(chatRoomId, participantUuid);
        chatSinkManager.addParticipant(chatRoomId, participantUuid);

        Sinks.Many<ResponseChatMessageDto> sink = chatSinkManager.getOrCreateSink(chatRoomId);

        Flux<ResponseChatMessageDto> messageFlux = sink.asFlux();

        Flux<ResponseChatMessageDto> pingFlux = Flux.interval(Duration.ofSeconds(10))
                .map(tick -> ResponseChatMessageDto.ping(chatRoomId));

        return Flux.merge(messageFlux, pingFlux)
                .doOnSubscribe(sub -> log.info("👀 SSE 구독 시작: chatRoomId={}, participantUuid={}", chatRoomId, participantUuid))
                .doFinally(signalType -> {
                    log.info("❌ SSE 종료 감지: {}, chatRoomId={}, participantUuid={}", signalType, chatRoomId, participantUuid);
                    redisUtil.removeParticipantFromChatRoom(chatRoomId, participantUuid);
                    chatSinkManager.removeParticipant(chatRoomId, participantUuid);
                });
    }

    /**
     * 채팅방 ID로 이전 메시지 조회
     *
     * @param chatRoomId
     */
    @Override
    public Mono<List<ResponseChatMessageDto>> getPreviousChatMessageByChatRoomId(String chatRoomId, String participantUuid) {
        // 1. read = false인 메시지들 read = true로 수정
        Mono<Void> markRead = chatMessageReactiveRepository
                .findUnreadMessagesByChatRoomIdAndNotSender(chatRoomId, participantUuid)
                .flatMap(message -> {
                    if (!message.isRead()) {
                        message.markAsRead();
                        return chatMessageReactiveRepository.save(message)
                                .doOnSuccess(updated -> {
                                    // ✅ 여기서 직접 emitToSink 처리
                                    ResponseChatMessageDto dto = ResponseChatMessageDto.from(updated);
                                    emitToSink(chatRoomId, dto);
                                });
                    }
                    return Mono.empty();
                })
                .then();

        // 2. unread count 초기화
        Mono<Void> resetUnread = Mono.fromRunnable(() ->
                chatRoomService.resetUnreadCount(chatRoomId, participantUuid));

        // 3. 메시지 전체 조회
        Mono<List<ResponseChatMessageDto>> messages = chatMessageReactiveRepository
                .findAllByChatRoomIdOrderBySentAtDesc(chatRoomId)
                .map(ResponseChatMessageDto::from)
                .collectList();

        // 4. read 처리 + count 초기화 + 메시지 조회를 순차적으로 실행
        return markRead.then(resetUnread).then(messages);
    }

    @Override
    public void emitToSink(String chatRoomId, ResponseChatMessageDto responseChatMessageDto) {
        chatSinkManager.emitToSink(chatRoomId, responseChatMessageDto);
    }

    @Override
    public boolean isParticipantOnline(String chatRoomId, String participantUuid) {
        return redisUtil.isParticipantOnline(chatRoomId, participantUuid);
    }

}
