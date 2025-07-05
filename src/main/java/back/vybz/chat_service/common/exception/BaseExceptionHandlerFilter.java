package back.vybz.chat_service.common.exception;

import back.vybz.chat_service.common.entity.BaseResponseEntity;
import back.vybz.chat_service.common.entity.BaseResponseStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.security.sasl.AuthenticationException;

@Slf4j
@Component
@Order(-1)
public class BaseExceptionHandlerFilter implements WebFilter {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
                .onErrorResume(BaseException.class, e -> {
                    log.error("BaseException -> {}({})", e.getStatus(), e.getStatus().getMessage(), e);
                    return handleBaseException(exchange, e);
                })
                .onErrorResume(AuthenticationException.class, e -> {
                    log.error("AuthenticationException -> {}", e.getMessage(), e);
                    return handleBaseException(exchange, new BaseException(BaseResponseStatus.NO_SIGN_IN));
                });
    }

    private Mono<Void> handleBaseException(ServerWebExchange exchange, BaseException be) {
        BaseResponseEntity baseResponse = new BaseResponseEntity(be.getStatus());
        
        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        try {
            String responseBody = objectMapper.writeValueAsString(baseResponse);
            return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory().wrap(
                            responseBody.getBytes()
                    ))
            );
        } catch (Exception e) {
            log.error("Error writing response", e);
            return Mono.error(e);
        }
    }
}
