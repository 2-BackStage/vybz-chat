package back.vybz.chat_service.common.exception;

import back.vybz.chat_service.common.entity.BaseResponseEntity;
import back.vybz.chat_service.common.entity.BaseResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(-2)
public class BaseExceptionHandler implements WebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (ex instanceof BaseException) {
            return handleBaseException(exchange, (BaseException) ex);
        } else if (ex instanceof WebExchangeBindException) {
            return handleValidationException(exchange, (WebExchangeBindException) ex);
        } else if (ex instanceof NoResourceFoundException) {
            return handleNoResourceFoundException(exchange, (NoResourceFoundException) ex);
        } else if (ex instanceof RuntimeException) {
            return handleRuntimeException(exchange, (RuntimeException) ex);
        }
        
        return Mono.error(ex);
    }

    private Mono<Void> handleBaseException(ServerWebExchange exchange, BaseException e) {
        BaseResponseEntity<Void> response = new BaseResponseEntity<>(e.getStatus());
        log.error("BaseException -> {}({})", e.getStatus(), e.getStatus().getMessage(), e);
        
        exchange.getResponse().setStatusCode(HttpStatus.valueOf(response.httpStatus().value()));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(
                        response.toString().getBytes()
                ))
        );
    }

    private Mono<Void> handleValidationException(ServerWebExchange exchange, WebExchangeBindException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> String.format("%s : %s", fieldError.getField(), fieldError.getDefaultMessage()))
                .orElse("잘못된 요청입니다.");

        log.warn("Validation failed: {}", errorMessage);

        BaseResponseEntity<Void> response = new BaseResponseEntity<>(
                BaseResponseStatus.INVALID_REQUEST.getHttpStatusCode(),
                false,
                errorMessage,
                BaseResponseStatus.INVALID_REQUEST.getCode(),
                null
        );

        exchange.getResponse().setStatusCode(HttpStatus.valueOf(response.httpStatus().value()));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(
                        response.toString().getBytes()
                ))
        );
    }

    private Mono<Void> handleNoResourceFoundException(ServerWebExchange exchange, NoResourceFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        
        BaseResponseEntity<Void> response = new BaseResponseEntity<>(
                BaseResponseStatus.NOT_FOUND.getHttpStatusCode(),
                false,
                "요청한 리소스를 찾을 수 없습니다.",
                BaseResponseStatus.NOT_FOUND.getCode(),
                null
        );

        exchange.getResponse().setStatusCode(HttpStatus.valueOf(response.httpStatus().value()));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(
                        response.toString().getBytes()
                ))
        );
    }

    private Mono<Void> handleRuntimeException(ServerWebExchange exchange, RuntimeException e) {
        BaseResponseEntity<Void> response = new BaseResponseEntity<>(BaseResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        log.error("RuntimeException: ", e);
        
        exchange.getResponse().setStatusCode(HttpStatus.valueOf(response.httpStatus().value()));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(
                        response.toString().getBytes()
                ))
        );
    }
}
