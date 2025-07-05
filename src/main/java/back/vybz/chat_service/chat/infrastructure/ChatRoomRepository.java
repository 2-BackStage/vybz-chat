package back.vybz.chat_service.chat.infrastructure;

import back.vybz.chat_service.chat.domain.ChatRoom;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ChatRoomRepository extends ReactiveMongoRepository<ChatRoom, String>, ChatRoomRepositoryCustom {

    /**
     * 참여자 2명의 UUID를 기준으로 채팅방 조회
     * @param participantUuid
     * @param opponentUuid
     */
    @Query("""
            {
              'participant.participantUuid': { $all: [?0, ?1] },
              'participant': { $size: 2 }
            }
            """)
    Flux<ChatRoom> findAllChatRoomByTwoParticipants(String participantUuid, String opponentUuid);

}
