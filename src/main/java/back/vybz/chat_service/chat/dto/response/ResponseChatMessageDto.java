package back.vybz.chat_service.chat.dto.response;

import back.vybz.chat_service.chat.domain.ChatMessage;
import back.vybz.chat_service.chat.domain.MessageType;
import back.vybz.chat_service.chat.vo.response.ResponseChatMessageVo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Getter
@NoArgsConstructor
public class ResponseChatMessageDto {

    private String senderUuid;
    private MessageType messageType;
    private String content;
    private boolean read;
    private Instant sentAt;

    @Builder
    public ResponseChatMessageDto(String senderUuid, MessageType messageType, String content, boolean read, Instant sentAt) {
        this.senderUuid = senderUuid;
        this.messageType = messageType;
        this.content = content;
        this.read = read;
        this.sentAt = sentAt;
    }

    public static ResponseChatMessageDto from(ChatMessage chatMessage) {
        return ResponseChatMessageDto.builder()
                .senderUuid(chatMessage.getSenderUuid())
                .messageType(chatMessage.getMessageType())
                .content(chatMessage.getContent())
                .read(chatMessage.isRead())
                .sentAt(chatMessage.getSentAt())
                .build();
    }

    public ResponseChatMessageVo toVo() {

        ZonedDateTime kstTime = this.sentAt
                .atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.of("Asia/Seoul"));

        return ResponseChatMessageVo.builder()
                .senderUuid(senderUuid)
                .messageType(messageType)
                .content(content)
                .read(read)
                .sentAt(kstTime.toLocalDateTime())
                .build();
    }

}
