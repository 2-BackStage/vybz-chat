package back.vybz.chat_service.chat.infrastructure;

import back.vybz.chat_service.chat.domain.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String>, ChatRoomRepositoryCustom {

    /**
     * 성능 최적화: 단순한 쿼리로 모든 채팅방 조회 후 Java에서 필터링
     */
    @Query("""
            {
              'participant.participantUuid': { $all: [?0, ?1] },
              'participant': { $size: 2 }
            }
            """)
    List<ChatRoom> findAllChatRoomByTwoParticipants(String participantUuid, String opponentUuid);

}
