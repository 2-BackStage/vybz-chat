package back.vybz.chat_service.chat.infrastructure;

import back.vybz.chat_service.chat.domain.ChatMessage;
import back.vybz.chat_service.common.util.MongoCursorHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatMessageReactiveRepositoryCustomImpl implements ChatMessageReactiveRepositoryCustom {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    /**
     * 채팅 내역 조회(커서 기반)
     * @param chatRoomId
     * @param sentAt
     * @param pageSize
     */
    @Override
    public Flux<ChatMessage> findByChatRoomIdWithCursor(String chatRoomId, Instant sentAt, Integer pageSize) {
        Query query = new Query();
        query.addCriteria(Criteria.where("chatRoomId").is(chatRoomId));
        Query finalQuery = MongoCursorHelper.build(query, "sentAt", sentAt, pageSize, Sort.Direction.DESC);
        return reactiveMongoTemplate.find(finalQuery, ChatMessage.class);
    }

    /**
     * 채팅 내역 조회(커서 기반, 나갔다 들어온 경우)
     * @param chatRoomId
     * @param sentAt
     * @param pageSize
     */
    @Override
    public Flux<ChatMessage> findByChatRoomIdWithCursorAndAfterLeft(
            String chatRoomId, Instant leftAt, Instant sentAt, Integer pageSize) {
        Criteria criteria = Criteria.where("chatRoomId").is(chatRoomId);
        List<Criteria> timeCriteria = new ArrayList<>();
        if (leftAt != null) {
            timeCriteria.add(Criteria.where("sentAt").gt(leftAt));
        }
        if (sentAt != null) {
            timeCriteria.add(Criteria.where("sentAt").gt(sentAt));
        }
        if (!timeCriteria.isEmpty()) {
            criteria = new Criteria().andOperator(criteria, new Criteria().andOperator(timeCriteria.toArray(new Criteria[0])));
        }
        Query query = new Query(criteria);
        Query finalQuery = MongoCursorHelper.build(query, "sentAt", sentAt, pageSize, Sort.Direction.DESC);
        return reactiveMongoTemplate.find(finalQuery, ChatMessage.class);
    }
}
