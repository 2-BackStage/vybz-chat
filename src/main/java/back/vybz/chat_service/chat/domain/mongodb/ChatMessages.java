package back.vybz.chat_service.chat.domain.mongodb;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Getter
@NoArgsConstructor
@Document
public class ChatMessages {

    @Id
    private ObjectId id;

    //채팅방 id
    @Field(name = "chat_room_id")
    private ObjectId chatRoomId;

    //보낸사람 id
    @Field(name = "sender_id")
    private String senderId;

    //보낸사람 타입
    @Field(name = "sender_type")
    private SenderType senderType;

    //보낸내용
    @Field(name = "content")
    private String content;

    //읽음여부
    @Field(name = "is_read")
    private boolean isRead;

    @CreatedDate
    @Field(name = "created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field(name = "updated_at")
    private Instant updatedAt;

    @Builder
    public ChatMessages(ObjectId chatRoomId,
                        String senderId,
                        SenderType senderType,
                        String content,
                        boolean isRead) {
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.senderType = senderType;
        this.content = content;
        this.isRead = isRead;
    }
}
