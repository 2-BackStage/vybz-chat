package back.vybz.chat_service.kafka.producer;

import back.vybz.chat_service.kafka.event.ChatEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatKafkaProducer {

    private final KafkaTemplate<String, ChatEvent> kafkaTemplate;

    private final String topicName = "chat-message";

    public void sendChatMessage(ChatEvent event) {

        log.info("📤 [Kafka] Sending LiveChatEvent to '{}': {}", topicName, event);

        CompletableFuture<SendResult<String, ChatEvent>> future =
                kafkaTemplate.send(topicName, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("❌ [Kafka] Failed to send ChatEvent: {}", ex.getMessage(), ex);
            } else {
                log.info("✅ [Kafka] Sent ChatEvent to partition {} with offset {}",
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }


}
