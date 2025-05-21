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
public class ChatRooms {

    @Id
    private ObjectId id;

    //보낸사람 id
    @Field(name = "sender_id")
    private String senderId;

    //사용자 uuid
    @Field(name = " user_uuid")
    private String userUuid;

    //버스커 UUID
    @Field(name = "busker_uuid")
    private String buskerUuid;

    //사용자가 보낸 메시지 수
    @Field(name = "unread_count_by_user")
    private int unreadCountByUser;

    //버스커가 보낸 메시지 수
    @Field(name = "unread_count_by_busker")
    private int unreadCountByBusker;

    //안읽은 메시지
    @Field(name = "last_message")
    private LastMessage lastMessage;

    @CreatedDate
    @Field(name = "created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field(name = "updated_at")
    private Instant updatedAt;

    @Builder
    public ChatRooms(String senderId,
                     String userUuid,
                     String buskerUuid,
                     int unreadCountByUser,
                     int unreadCountByBusker,
                     LastMessage lastMessage) {
        this.senderId = senderId;
        this.userUuid = userUuid;
        this.buskerUuid = buskerUuid;
        this.unreadCountByUser = unreadCountByUser;
        this.unreadCountByBusker = unreadCountByBusker;
        this.lastMessage = lastMessage;
    }




}
