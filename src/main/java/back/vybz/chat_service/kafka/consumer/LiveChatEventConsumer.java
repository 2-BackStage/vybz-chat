package back.vybz.chat_service.kafka.consumer;

import back.vybz.chat_service.kafka.event.LiveChatEvent;
import back.vybz.chat_service.live_chat.dto.response.ResponseLiveChatDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LiveChatEventConsumer {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @KafkaListener(
            topics = "live-chat",
            groupId = "live-chat-group",
            containerFactory = "liveChatKafkaListenerContainerFactory"
    )
    public void consume(LiveChatEvent event) {
        log.info("🟡 Kafka 수신 이벤트: {}", event);

        ResponseLiveChatDto response = ResponseLiveChatDto.from(event);

        simpMessagingTemplate.convertAndSend("/topic/live-chat/" + event.getLiveId(), response);
        log.info("🟢 브로드캐스트 완료: /topic/live-chat/{}", event.getLiveId());
    }

}
