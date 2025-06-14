package back.vybz.chat_service.chat.infrastructure;

import back.vybz.chat_service.chat.domain.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    /**
     * 채팅방 ID를 기준으로 메시지 목록 조회 (내림차순 정렬)
     * @param chatRoomId
     */
    List<ChatMessage> findAllByChatRoomIdOrderBySentAtDesc(String chatRoomId);

}
