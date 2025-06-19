package back.vybz.chat_service.kafka.config;

import back.vybz.chat_service.kafka.event.ChatEvent;
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
public class ChatKafkaConfig {

    private final CommonKafkaConfig commonKafkaConfig;

    @Bean
    public ProducerFactory<String, ChatEvent> chatProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonKafkaConfig.commonProducerConfigs());
    }

    @Bean
    public KafkaTemplate<String, ChatEvent> chatKafkaTemplate() {
        return new KafkaTemplate<>(chatProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, ChatEvent> chatConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
                commonKafkaConfig.commonConsumerConfigs(),
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(new JsonDeserializer<>(ChatEvent.class, false))
        );
    }

    @Bean(name = "chatKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, ChatEvent> chatKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ChatEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(chatConsumerFactory());
        return factory;
    }

}
