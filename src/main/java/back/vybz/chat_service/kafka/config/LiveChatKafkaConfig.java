package back.vybz.chat_service.kafka.config;

import back.vybz.chat_service.kafka.event.LiveChatEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
@RequiredArgsConstructor
public class LiveChatKafkaConfig {

    private final CommonKafkaConfig commonKafkaConfig;

    @Bean
    public ProducerFactory<String, LiveChatEvent> liveChatProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonKafkaConfig.commonProducerConfigs());
    }

    @Bean
    public KafkaTemplate<String, LiveChatEvent> liveChatKafkaTemplate() {
        return new KafkaTemplate<>(liveChatProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, LiveChatEvent> liveChatConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
                commonKafkaConfig.commonConsumerConfigs(),
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(new JsonDeserializer<>(LiveChatEvent.class, false))
        );
    }

    @Bean(name = "liveChatKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, LiveChatEvent> liveChatKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, LiveChatEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(liveChatConsumerFactory());
        return factory;
    }

}
