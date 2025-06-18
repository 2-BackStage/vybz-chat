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
    public Flux<ChatMessage> findByChatRoomIdWithCursorAndAfterLeft(String chatRoomId, Instant sentAt, Integer pageSize) {
        Criteria criteria = Criteria.where("chatRoomId").is(chatRoomId);
        if (sentAt != null) {
            criteria = new Criteria().andOperator(criteria, Criteria.where("sentAt").gt(sentAt));
        }
        Query query = new Query(criteria);
        Query finalQuery = MongoCursorHelper.build(query, "sentAt", sentAt, pageSize, Sort.Direction.DESC);
        return reactiveMongoTemplate.find(finalQuery, ChatMessage.class);
    }
}
