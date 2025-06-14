package back.vybz.chat_service.chat.vo.response;

import back.vybz.chat_service.chat.domain.MessageType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ResponseChatMessageVo {

    private String senderUuid;
    private MessageType messageType;
    private String content;
    private boolean read;
    private LocalDateTime sentAt;

    @Builder
    public ResponseChatMessageVo(String senderUuid, MessageType messageType, String content, boolean read, LocalDateTime sentAt) {
        this.senderUuid = senderUuid;
        this.messageType = messageType;
        this.content = content;
        this.read = read;
        this.sentAt = sentAt;
    }

}
