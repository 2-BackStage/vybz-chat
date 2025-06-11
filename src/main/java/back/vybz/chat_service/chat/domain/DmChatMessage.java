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
@Document(collection = "dm_chat_message")
public class DmChatMessage {

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
     * 수신자 UUID
     */
    @Field(value = "receiver_uuid")
    private String receiverUuid;

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
    private Boolean read;

    /**
     * 메시지 전송 시간
     */
    @CreatedDate
    @Field(value = "sent_at")
    private Instant sentAt;

    @Builder
    public DmChatMessage(String id, String chatRoomId, String senderUuid, String receiverUuid, MessageType messageType, String content, Boolean read, Instant sentAt) {
        this.id = id;
        this.chatRoomId = chatRoomId;
        this.senderUuid = senderUuid;
        this.receiverUuid = receiverUuid;
        this.messageType = messageType;
        this.content = content;
        this.read = read;
        this.sentAt = sentAt;
    }

}
