package back.vybz.chat_service.common.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class ReactiveRedisConfig {

    @Bean
    public ClientResources clientResources() {
        return DefaultClientResources.builder()
                .ioThreadPoolSize(4)  // IO 스레드 풀 크기
                .computationThreadPoolSize(4)  // 계산 스레드 풀 크기
                .build();
    }

    @Bean
    public ClientOptions clientOptions() {
        return ClientOptions.builder()
                .autoReconnect(true)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .build();
    }

    @Bean
    public LettuceClientConfiguration lettuceClientConfiguration(ClientResources clientResources, ClientOptions clientOptions) {
        return LettuceClientConfiguration.builder()
                .clientResources(clientResources)
                .clientOptions(clientOptions)
                .readFrom(ReadFrom.REPLICA_PREFERRED)  // 읽기는 레플리카 우선
                .commandTimeout(Duration.ofSeconds(2))
                .shutdownTimeout(Duration.ofSeconds(2))
                .build();
    }

    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        RedisSerializationContext<String, String> context = RedisSerializationContext
                .<String, String>newSerializationContext(new StringRedisSerializer())
                .hashValue(new StringRedisSerializer())
                .build();
        
        return new ReactiveRedisTemplate<>(factory, context);
    }

}
