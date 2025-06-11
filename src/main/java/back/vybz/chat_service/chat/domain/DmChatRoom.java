package back.vybz.chat_service.chat.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Getter
@NoArgsConstructor
@Document(collection = "dm_chat_room")
public class DmChatRoom {

    @Id
    private String id;

    /**
     * 유저 uuid
     */
    @Field(name = "user_uuid")
    private String userUuid;

    /**
     * 버스커 uuid
     */
    @Field(name = "busker_uuid")
    private String buskerUuid;

    /**
     * 유저의 읽지 않은 메시지 수
     */
    @Field(name = "user_unread_count")
    private Integer userUnreadCount;

    /**
     * 버스커의 읽지 않은 메시지 수
     */
    @Field(name = "busker_unread_count")
    private Integer buskerUnreadCount;

    /**
     * 마지막 메시지 정보
     */
    @Field(name = "last_message")
    private LastMessage lastMessage;

    @CreatedDate
    @Field(name = "created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Field(name = "updated_at")
    private Instant updatedAt;

    @Builder
    public DmChatRoom(String id, String userUuid, String buskerUuid, Integer userUnreadCount, Integer buskerUnreadCount, LastMessage lastMessage, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userUuid = userUuid;
        this.buskerUuid = buskerUuid;
        this.userUnreadCount = userUnreadCount;
        this.buskerUnreadCount = buskerUnreadCount;
        this.lastMessage = lastMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
