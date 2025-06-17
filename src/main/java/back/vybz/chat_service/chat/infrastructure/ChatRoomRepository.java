package back.vybz.chat_service.chat.infrastructure;

import back.vybz.chat_service.chat.domain.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String>, ChatRoomRepositoryCustom {

    /**
     * 두 사용자의 UUID를 기준으로 DM 채팅방 조회
     * @param participantUuid
     * @param opponentUuid
     */
    @Query("{ 'participant.participantUuid': { $all: [?0, ?1] }, 'participant': { $size: 2 } }")
    Optional<ChatRoom> findChatRoomByTwoParticipants(String participantUuid, String opponentUuid);

    /**
     * 특정 사용자의 UUID를 기준으로 DM 채팅방 목록 조회
     * @param participantUuid
     */
    @Query(value = "{ 'participant': { $elemMatch: { 'participantUuid': ?0, 'hidden': false } } }")
    List<ChatRoom> findAllByParticipantUserUuidAndHiddenFalse(String participantUuid);

}
