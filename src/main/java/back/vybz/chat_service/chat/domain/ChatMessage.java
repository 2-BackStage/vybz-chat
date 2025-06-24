package back.vybz.chat_service.chat.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Getter
@NoArgsConstructor
@Document(collection = "chat_message")
@CompoundIndexes({
        // 채팅방 내 메시지 조회용 (커서 기반, 내림차순)
        @CompoundIndex(name = "chatRoomId_sentAt_idx", def = "{'chat_room_id': 1, 'sent_at': -1}"),

        // 읽지 않은 메시지 조회용
        @CompoundIndex(name = "chatRoomId_receiverUuid_read_idx", def = "{'chat_room_id': 1, 'receiver_uuid': 1, 'read': 1}"),

        // 사용자 퇴장 이후 메시지 조회용
        @CompoundIndex(name = "chatRoomId_sentAt_cursor_idx", def = "{'chat_room_id': 1, 'sent_at': 1}"),

        // LEFT 메시지 확인용 (가장 마지막 LEFT 메시지 조회)
        @CompoundIndex(name = "chatRoomId_senderUuid_type_sentAt_idx", def = "{'chat_room_id': 1, 'sender_uuid': 1, 'message_type': 1, 'sent_at': -1}")
})
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
    public ChatMessage(String id, String chatRoomId, String senderUuid, String receiverUuid, MessageType messageType, String content, boolean read, Instant sentAt) {
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
