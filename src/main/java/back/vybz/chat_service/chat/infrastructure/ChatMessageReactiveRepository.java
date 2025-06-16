package back.vybz.chat_service.chat.infrastructure;

import back.vybz.chat_service.chat.domain.ChatMessage;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface ChatMessageReactiveRepository extends ReactiveMongoRepository<ChatMessage, String> {

    /**
     * 채팅방 메시지 스트림으로 내려받기
     * @param chatRoomId
     */
    @Tailable
    @Query("{ 'chatRoomId': ?0 }")
    Flux<ChatMessage> findChatMessageByChatRoomId(String chatRoomId);

    /**
     * 채팅방 ID와 읽지 않은 메시지 조회 (본인이 아닌 상대방이 보낸 것)
     * @param chatRoomId
     * @param receiverUuid
     */
    @Query("{ 'chatRoomId': ?0, 'senderUuid': { $ne: ?1 }, 'read': false }")
    Flux<ChatMessage> findUnreadMessagesByChatRoomIdAndNotSender(String chatRoomId, String receiverUuid);

    /**
     * 채팅방 ID로 메시지 목록 조회 (내림차순 정렬)
     * @param chatRoomId
     */
    Flux<ChatMessage> findAllByChatRoomIdOrderBySentAtDesc(String chatRoomId);

}
