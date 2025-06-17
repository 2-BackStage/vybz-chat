package back.vybz.chat_service.chat.infrastructure;

import back.vybz.chat_service.chat.domain.ChatMessage;
import reactor.core.publisher.Flux;

import java.time.Instant;

public interface ChatMessageReactiveRepositoryCustom {

    /**
     * 채팅 내역 조회(커서 기반)
     * @param chatRoomId
     * @param sentAt
     * @param pageSize
     */
    Flux<ChatMessage> findByChatRoomIdWithCursor(String chatRoomId, Instant sentAt, Integer pageSize);

    /**
     * 채팅 내역 조회(커서 기반, 나갔다 들어온 경우)
     * @param chatRoomId
     * @param sentAt
     * @param pageSize
     */
    Flux<ChatMessage> findByChatRoomIdWithCursorAndAfterLeft(String chatRoomId, Instant sentAt, Integer pageSize);

}
