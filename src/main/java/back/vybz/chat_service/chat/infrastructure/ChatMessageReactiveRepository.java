package back.vybz.chat_service.chat.infrastructure;

import back.vybz.chat_service.chat.domain.ChatMessage;
import back.vybz.chat_service.chat.domain.MessageType;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
public interface ChatMessageReactiveRepository extends ReactiveMongoRepository<ChatMessage, String> {

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

    /**
     * 채팅방 ID와 특정 시간 이후의 메시지 목록 조회 (내림차순 정렬)
     * @param chatRoomId
     * @param sentAt
     */
    Flux<ChatMessage> findAllByChatRoomIdAndSentAtAfterOrderBySentAtDesc(String chatRoomId, Instant sentAt);

    /**
     * 사용자가 채팅방에서 마지막으로 퇴장한 메시지 조회 (최근 1건)
     * @param chatRoomId
     * @param senderUuid
     * @param messageType
     */
    Mono<ChatMessage> findFirstByChatRoomIdAndSenderUuidAndMessageTypeOrderBySentAtDesc(String chatRoomId, String senderUuid, MessageType messageType);

}
