package back.vybz.chat_service.chat.dto.response;

import back.vybz.chat_service.chat.domain.ChatMessage;
import back.vybz.chat_service.chat.domain.MessageType;
import back.vybz.chat_service.chat.vo.response.ResponseChatMessageVo;
import back.vybz.chat_service.kafka.event.ChatEvent;
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

    private String id;
    private String chatRoomId;
    private String senderUuid;
    private MessageType messageType;
    private String content;
    private boolean read;
    private Instant sentAt;

    @Builder
    public ResponseChatMessageDto(String id, String chatRoomId, String senderUuid, MessageType messageType, String content, boolean read, Instant sentAt) {
        this.id = id;
        this.chatRoomId = chatRoomId;
        this.senderUuid = senderUuid;
        this.messageType = messageType;
        this.content = content;
        this.read = read;
        this.sentAt = sentAt;
    }

    public static ResponseChatMessageDto from(ChatMessage chatMessage) {
        return ResponseChatMessageDto.builder()
                .id(chatMessage.getId())
                .chatRoomId(chatMessage.getChatRoomId())
                .senderUuid(chatMessage.getSenderUuid())
                .messageType(chatMessage.getMessageType())
                .content(chatMessage.getContent())
                .read(chatMessage.isRead())
                .sentAt(chatMessage.getSentAt())
                .build();
    }

    public static ResponseChatMessageDto fromEvent(ChatEvent chatEvent) {
        return ResponseChatMessageDto.builder()
                .chatRoomId(chatEvent.getChatRoomId())
                .senderUuid(chatEvent.getSenderUuid())
                .messageType(chatEvent.getMessageType())
                .content(chatEvent.getContent())
                .read(chatEvent.isRead())
                .sentAt(chatEvent.getSentAt())
                .build();
    }

    public static ResponseChatMessageDto ping(String chatRoomId) {
        return ResponseChatMessageDto.builder()
                .id("ping")
                .chatRoomId(chatRoomId)
                .senderUuid("system")
                .messageType(MessageType.SYSTEM)
                .content("ping")
                .read(true)
                .sentAt(Instant.now())
                .build();
    }

    public ResponseChatMessageVo toVo() {
        ZonedDateTime kstTime = this.sentAt
                .atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        return ResponseChatMessageVo.builder()
                .id(id)
                .senderUuid(senderUuid)
                .messageType(messageType)
                .content(content)
                .read(read)
                .sentAt(kstTime.toLocalDateTime())
                .build();
    }

}
