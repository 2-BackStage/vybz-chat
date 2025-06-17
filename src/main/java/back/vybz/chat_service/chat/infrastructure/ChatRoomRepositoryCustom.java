package back.vybz.chat_service.chat.infrastructure;

import back.vybz.chat_service.chat.domain.ChatRoom;

import java.time.Instant;
import java.util.List;

public interface ChatRoomRepositoryCustom {

    /**
     * 특정 사용자의 UUID를 기준으로 채팅방 목록 조회 (커서 기반 페이징)
     * @param participantUuid
     * @param sentAt
     * @param pageSize
     */
    List<ChatRoom> findByParticipantUuidWithCursor(String participantUuid, Instant sentAt, Integer pageSize);

}
