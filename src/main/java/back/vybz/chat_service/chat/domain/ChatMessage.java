package back.vybz.chat_service.chat.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Getter
@NoArgsConstructor
@Document(collection = "chat_message")
public class ChatMessage {

    @Id
    private String id;

    /**
     * DM 채팅방 ID
     */
    @Field(value = "chat_room_id")
    private String chatRoomId;

    /**
     * 발신자 UUID
     */
    @Field(value = "sender_uuid")
    private String senderUuid;

    /**
     * 메시지 타입
     */
    @Field(value = "message_type")
    private MessageType messageType;

    /**
     * 메시지 내용
     */
    @Field(value = "content")
    private String content;

    /**
     * 읽음 여부
     */
    @Field(value = "read")
    private boolean read = false;

    /**
     * 메시지 전송 시간
     */
    @CreatedDate
    @Field(value = "sent_at")
    private Instant sentAt;

    public void markAsRead() {
        this.read = true;
    }

    @Builder
    public ChatMessage(String id, String chatRoomId, String senderUuid, MessageType messageType, String content, boolean read, Instant sentAt) {
        this.id = id;
        this.chatRoomId = chatRoomId;
        this.senderUuid = senderUuid;
        this.messageType = messageType;
        this.content = content;
        this.read = read;
        this.sentAt = sentAt;
    }

}
