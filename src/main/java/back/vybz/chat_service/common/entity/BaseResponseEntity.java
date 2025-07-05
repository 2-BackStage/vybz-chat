package back.vybz.chat_service.common.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public record BaseResponseEntity<T>(
    @Schema(hidden = true) HttpStatusCode httpStatus, 
    @JsonProperty("isSuccess") Boolean isSuccess, 
    @JsonProperty("message") String message, 
    @JsonProperty("code") int code, 
    @JsonProperty("result") T result) {

    /**
     * 필요값 : Http상태코드, 성공여부, 메시지, 에러코드, 결과값
     * 1. Return 객체가 필요한 경우 -> 성공
     * 2. Return 객체가 필요 없는 경우 -> 성공
     * 3. 요청에 실패한 경우
     */

    /**
     * 1. Return 객체가 필요한 경우 -> 성공
     * @param result
     */
    public BaseResponseEntity(T result) {
        this(HttpStatus.OK, true, "SUCCESS", 200, result);
    }

    /**
     * 2. Return 객체가 필요 없는 경우 -> 성공
     */
    public BaseResponseEntity() {
        this(HttpStatus.OK, true, "SUCCESS", 200, null);
    }

    /**
     * 3. 요청에 실패한 경우
     * @param status
     */
    public BaseResponseEntity(BaseResponseStatus status) {
        this(status.getHttpStatusCode(), status.isSuccess(), status.getMessage(), status.getCode(), null);
    }

    public BaseResponseEntity(BaseResponseStatus status, String message) {
        this(status.getHttpStatusCode(), status.isSuccess(), message, status.getCode(), null);
    }

    /**
     * Return 객체가 필요하고 커스텀 상태값이 필요한 경우 -> 성공
     * @param message
     * @param result
     */
    public BaseResponseEntity(BaseResponseStatus base, T result) {
        this(base.getHttpStatusCode(),
                base.isSuccess(),
                base.getMessage(),
                base.getCode(),
                result
        );
    }

    @Override
    public String toString() {
        return String.format("{\"isSuccess\":%s,\"message\":\"%s\",\"code\":%d,\"result\":%s}", 
            isSuccess, message, code, result != null ? result.toString() : "null");
    }
}
