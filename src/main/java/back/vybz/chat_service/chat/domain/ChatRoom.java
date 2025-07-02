package back.vybz.chat_service.chat.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

@Getter
@NoArgsConstructor
@Document(collection = "chat_room")
@CompoundIndex(
        name = "participant_uuid_hidden_sentAt_idx",
        def = "{'participant.participantUuid': 1, 'participant.hidden': 1, 'last_message.sentAt': -1}"
)
public class ChatRoom {

    @Id
    private String id;

    /**
     * 참여자
     */
    @Field(name = "participant")
    private List<Participant> participant;

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

    public void updateLastMessage(ChatMessage message) {
        this.lastMessage = LastMessage.builder()
                .content(message.getContent())
                .messageType(message.getMessageType())
                .sentAt(message.getSentAt())
                .build();
    }

    public void increaseUnreadCountExcept(String senderUuid) {
        this.participant.forEach(p -> {
            if (!p.getParticipantUuid().equals(senderUuid)) {
                p.incrementUnreadCount();
            }
        });
    }

    public void resetUnreadCount(String participantUuid) {
        this.participant.forEach(p -> {
            if (p.getParticipantUuid().equals(participantUuid)) {
                p.resetUnreadCount();
            }
        });
    }

    @Builder
    public ChatRoom(String id, List<Participant> participant, LastMessage lastMessage, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.participant = participant;
        this.lastMessage = lastMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
