package back.vybz.chat_service.chat.infrastructure;

import back.vybz.chat_service.chat.domain.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String>, ChatRoomRepositoryCustom {



    /**
     * 두 사용자의 UUID를 기준으로 모든 DM 채팅방 조회 (hidden 상태 포함)
     *
     * @param participantUuid
     * @param opponentUuid
     */
    @Query("""
            {
              'participant': {
                $all: [
                  { $elemMatch: { 'participantUuid': ?0 } },
                  { $elemMatch: { 'participantUuid': ?1 } }
                ]
              },
              'participant': { $size: 2 }
            }
            """)
    List<ChatRoom> findAllChatRoomByTwoParticipants(String participantUuid, String opponentUuid);

    /**
     * 특정 사용자의 UUID를 기준으로 DM 채팅방 목록 조회
     *
     * @param participantUuid
     */
    @Query(value = "{ 'participant': { $elemMatch: { 'participantUuid': ?0, 'hidden': false } } }")
    List<ChatRoom> findAllByParticipantUserUuidAndHiddenFalse(String participantUuid);

}
