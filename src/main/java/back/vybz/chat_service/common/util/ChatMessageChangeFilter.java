package back.vybz.chat_service.common.util;

import back.vybz.chat_service.chat.domain.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChatMessageChangeFilter {

    /**
     * ChangeStreamEvent의 OperationType이 insert 또는 update인 경우에만 true를 반환
     */
    public boolean isRelevantOperation(ChangeStreamEvent<ChatMessage> event) {
        if (event.getRaw() == null || event.getRaw().getOperationType() == null) return false;
        String op = event.getRaw().getOperationType().getValue();
        return "insert".equals(op) || "update".equals(op);
    }

    /**
     * ChangeStreamEvent의 OperationType이 insert 또는 update이고, ChatMessage가 읽음 상태인 경우에만 true를 반환
     */
    public boolean shouldEmit(ChangeStreamEvent<ChatMessage> event) {
        String op = event.getRaw().getOperationType().getValue();
        ChatMessage body = event.getBody();
        if (body == null) return false;

        boolean emit = "insert".equals(op) || ("update".equals(op) && body.isRead());
        log.debug("🔎 필터링 조건: op={}, read={}, emit={}", op, body.isRead(), emit);
        return emit;
    }

}
