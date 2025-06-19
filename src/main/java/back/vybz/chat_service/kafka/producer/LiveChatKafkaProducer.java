package back.vybz.chat_service.kafka.producer;

import back.vybz.chat_service.kafka.event.LiveChatEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class LiveChatKafkaProducer {

    private final KafkaTemplate<String, LiveChatEvent> kafkaTemplate;

    private final String topicName = "live-chat";

    public void sendChatMessage(LiveChatEvent event) {
        String key = event.getLiveId();

        log.info("📤 [Kafka] Sending LiveChatEvent to '{}': {}", topicName, event);

        CompletableFuture<SendResult<String, LiveChatEvent>> future =
                kafkaTemplate.send(topicName, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("❌ [Kafka] Failed to send LiveChatEvent: {}", ex.getMessage(), ex);
            } else {
                log.info("✅ [Kafka] Sent LiveChatEvent to partition {} with offset {}",
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

}
