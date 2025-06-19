package back.vybz.chat_service.kafka.consumer;

import back.vybz.chat_service.chat.application.ChatRoomService;
import back.vybz.chat_service.chat.dto.response.ResponseChatMessageDto;
import back.vybz.chat_service.chat.infrastructure.ChatMessageReactiveRepository;
import back.vybz.chat_service.common.util.ChatSinkManager;
import back.vybz.chat_service.kafka.event.ChatEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatEventConsumer {

    private final ChatMessageReactiveRepository chatMessageReactiveRepository;
    private final ChatRoomService chatRoomService;
    private final ChatSinkManager chatSinkManager;

    @KafkaListener(
            topics = "chat-message",
            groupId = "chat-group",
            containerFactory = "chatKafkaListenerContainerFactory"
    )
    public void consumeChatMessageEvent(ChatEvent event) {
        log.info("📬 [Kafka] 채팅 메시지 이벤트 수신: {}", event);

        chatMessageReactiveRepository.save(event.toDocument())
                .doOnSuccess(saved ->{
                    log.info("✅ [Kafka] 채팅 메시지 저장 완료: {}", saved.getId());

                    chatRoomService.updateLastMessage(event.getChatRoomId(), saved);

                    if (!event.isRead()) {
                        chatRoomService.increaseUnreadCount(event.getChatRoomId(), event.getSenderUuid());
                    }
                })
                .doOnError(e -> log.error("❌ [Kafka] 채팅 메시지 저장 실패: {}, 에러: {}", event.getChatRoomId(), e.getMessage()))
                .subscribe();
    }

}
