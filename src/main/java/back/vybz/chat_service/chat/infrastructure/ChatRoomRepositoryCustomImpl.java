package back.vybz.chat_service.chat.infrastructure;

import back.vybz.chat_service.chat.domain.ChatRoom;
import back.vybz.chat_service.common.util.MongoCursorHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryCustomImpl implements ChatRoomRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    /**
     * 사용자 uuid를 기준으로 채팅방 목록 조회(커서 페이징)
     * @param participantUuid
     * @param sentAt
     * @param pageSize
     */
    @Override
    public List<ChatRoom> findByParticipantUuidWithCursor(String participantUuid, Instant sentAt, Integer pageSize) {
        Query query = new Query();
        query.addCriteria(Criteria.where("participant")
                .elemMatch(Criteria.where("participantUuid").is(participantUuid)
                        .and("hidden").is(false)));
        Query finalQuery = MongoCursorHelper.build(query, "lastMessage.sentAt", sentAt, pageSize, Sort.Direction.DESC);

        return mongoTemplate.find(finalQuery, ChatRoom.class);
    }
}
