package back.vybz.chat_service.kafka.event;

import back.vybz.chat_service.chat.domain.ChatMessage;
import back.vybz.chat_service.chat.domain.MessageType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
public class  ChatEvent {

    private String chatRoomId;
    private String senderUuid;
    private String receiverUuid;
    private String content;
    private MessageType messageType;
    private boolean read;
    private Instant sentAt;

    @Builder
    public ChatEvent(String chatRoomId, String senderUuid, String receiverUuid,
                     String content, MessageType messageType, boolean read, Instant sentAt) {
        this.chatRoomId = chatRoomId;
        this.senderUuid = senderUuid;
        this.receiverUuid = receiverUuid;
        this.content = content;
        this.messageType = messageType;
        this.read = read;
        this.sentAt = sentAt;
    }

    public ChatMessage toDocument() {
        return ChatMessage.builder()
                .chatRoomId(chatRoomId)
                .senderUuid(senderUuid)
                .receiverUuid(receiverUuid)
                .messageType(messageType)
                .content(content)
                .read(read)
                .sentAt(sentAt)
                .build();
    }

}
