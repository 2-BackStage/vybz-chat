package back.vybz.chat_service.chat.application;

import back.vybz.chat_service.chat.domain.ChatMessage;
import back.vybz.chat_service.chat.domain.MessageType;
import back.vybz.chat_service.chat.dto.request.RequestLeaveChatRoomDto;
import back.vybz.chat_service.chat.dto.request.RequestSendMessageDto;
import back.vybz.chat_service.chat.dto.response.ResponseChatMessageDto;
import back.vybz.chat_service.chat.infrastructure.ChatMessageReactiveRepository;
import back.vybz.chat_service.common.util.ChatMessageChangeStreamListener;
import back.vybz.chat_service.common.util.CursorPageUtil;
import back.vybz.chat_service.common.util.RedisUtil;
import back.vybz.chat_service.kafka.producer.ChatKafkaProducer;
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
    private final ChatMessageChangeStreamListener chatMessageChangeStreamListener;
    private final ChatMessageReactiveRepository chatMessageReactiveRepository;
    private final ChatKafkaProducer chatKafkaProducer;


    /**
     * 메시지 전송 (Reactive 저장 + 마지막 메시지 갱신)
     * @param requestSendMessageDto
     */
    @Override
    public Mono<Void> sendMessage(RequestSendMessageDto requestSendMessageDto) {
        return redisUtil.isParticipantOnline(
                        requestSendMessageDto.getChatRoomId(),
                        requestSendMessageDto.getReceiverUuid()
                )
                .flatMap(receiverOnline -> {
                    chatRoomService.rejoinIfHidden(
                            requestSendMessageDto.getChatRoomId(),
                            List.of(requestSendMessageDto.getSenderUuid(), requestSendMessageDto.getReceiverUuid())
                    );
                    
                    ChatMessage message = ChatMessage.builder()
                            .chatRoomId(requestSendMessageDto.getChatRoomId())
                            .senderUuid(requestSendMessageDto.getSenderUuid())
                            .receiverUuid(requestSendMessageDto.getReceiverUuid())
                            .content(requestSendMessageDto.getContent())
                            .messageType(requestSendMessageDto.getMessageType())
                            .read(receiverOnline)
                            .sentAt(Instant.now())
                            .build();
                    
                    return chatMessageReactiveRepository.save(message)
                            .doOnSuccess(saved -> {
                                chatRoomService.updateLastMessage(saved.getChatRoomId(), saved);
                                if (!saved.isRead()) {
                                    chatRoomService.increaseUnreadCount(saved.getChatRoomId(), saved.getSenderUuid());
                                }
                                chatKafkaProducer.sendChatMessage(requestSendMessageDto.toChatEvent());
                            });
                })
                .then();
    }

    /**
     * 채팅방 ID로 메시지 스트림 구독 (Change Stream + Sink 혼용)
     * @param chatRoomId
     */
    @Override
    public Flux<ResponseChatMessageDto> subscribeChatMessageByChatRoomId(String chatRoomId, String participantUuid) {
        chatMessageChangeStreamListener.addParticipant(chatRoomId, participantUuid);

        // 1. Change Stream 기반 실시간 메시지 Flux
        Flux<ResponseChatMessageDto> messageFlux = chatMessageChangeStreamListener.subscribeToChatRoom(chatRoomId);

        // 2. Sink 기반 메시지 Flux (읽음 처리된 메시지용)
        Flux<ResponseChatMessageDto> sinkFlux = chatMessageChangeStreamListener.getOrCreateRoomSink(chatRoomId).asFlux();

        // 3. 5초마다 ping 전송 (연결 상태 확인용)
        Flux<ResponseChatMessageDto> pingFlux = Flux.interval(Duration.ofSeconds(5))
            .map(tick -> ResponseChatMessageDto.ping(chatRoomId));

        // 4. 합쳐서 반환
        return Flux.merge(messageFlux, sinkFlux, pingFlux)
            .doFinally(signalType -> {
                log.info("❌ SSE 종료 감지: {}, chatRoomId={}, participantUuid={}", signalType, chatRoomId, participantUuid);
                chatMessageChangeStreamListener.removeParticipant(chatRoomId, participantUuid);
            });
    }

    /**
     * 채팅방 ID로 이전 메시지 조회
     * @param chatRoomId
     */
    @Override
    public Mono<CursorPageUtil<ResponseChatMessageDto, Instant>> getPreviousChatMessageByChatRoomId(String chatRoomId, String participantUuid, Instant sentAt, Integer pageSize) {
        // 읽지 않은 메시지 읽음으로 표시하고 읽음 처리된 메시지들 수집
        return chatMessageReactiveRepository.findUnreadMessagesByChatRoomIdAndNotSender(chatRoomId, participantUuid)
            .flatMap(message -> {
                message.markAsRead();
                return chatMessageReactiveRepository.save(message)
                    .map(ResponseChatMessageDto::from);
            })
            .collectList()
            .flatMap(readMessages -> {
                // 읽음 처리된 메시지들을 즉시 프론트로 전송
                chatMessageChangeStreamListener.emitReadMessages(chatRoomId, readMessages);
                
                // 이후 기존 메시지 조회
                return resetUnreadCount(chatRoomId, participantUuid)
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
            });
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
                    return chatMessageReactiveRepository.save(message);
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
                .flatMap(leftMessage -> {
                    Instant leftAt = leftMessage.getSentAt();
                    return chatMessageReactiveRepository
                            .findByChatRoomIdWithCursorAndAfterLeft(chatRoomId, leftAt, sentAt, pageSize)
                            .map(ResponseChatMessageDto::from)
                            .collectList();
                })
                .switchIfEmpty(chatMessageReactiveRepository
                        .findByChatRoomIdWithCursor(chatRoomId, sentAt, pageSize)
                        .map(ResponseChatMessageDto::from)
                        .collectList());
    }

    /**
     * 채팅방 나가기 메시지 발행
     * @param requestLeaveChatRoomDto
     */
    @Override
    public Mono<Void> leaveChatRoomMessage(RequestLeaveChatRoomDto requestLeaveChatRoomDto) {
        return chatMessageReactiveRepository.save(requestLeaveChatRoomDto.toDocument()).then();
    }

    /**
     * 시스템 메시지 전송
     * @param chatRoomId
     * @param content
     */
    @Override
    public Mono<Void> sendSystemMessage(String chatRoomId, String content) {
        ChatMessage systemMessage = ChatMessage.builder()
                .chatRoomId(chatRoomId)
                .senderUuid("system")
                .receiverUuid(null)
                .messageType(MessageType.SYSTEM)
                .content(content)
                .read(true)
                .sentAt(Instant.now())
                .build();

        return chatMessageReactiveRepository.save(systemMessage)
                .doOnSuccess(savedMsg -> {
                    chatRoomService.updateLastMessage(chatRoomId, savedMsg);
                })
                .then();
    }

}
