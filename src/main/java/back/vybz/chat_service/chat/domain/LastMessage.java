package back.vybz.chat_service.chat.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Getter
@NoArgsConstructor
public class LastMessage {

    /**
     * 발신자 uuid
     */
    private String senderUuid;

    /**
     * 내용
     */
    private String content;

    /**
     * 메시지 타입
     */
    private MessageType messageType;

    /**
     * 메시지 전송 시간
     */
    @CreatedDate
    private Instant sentAt;

}
