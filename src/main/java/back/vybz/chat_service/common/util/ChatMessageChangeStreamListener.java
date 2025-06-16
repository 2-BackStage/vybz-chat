package back.vybz.chat_service.common.util;

import back.vybz.chat_service.chat.application.ChatMessageService;
import back.vybz.chat_service.chat.domain.ChatMessage;
import back.vybz.chat_service.chat.dto.response.ResponseChatMessageDto;
import com.mongodb.client.model.changestream.FullDocument;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageChangeStreamListener {

    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final ChatMessageService chatMessageService;

    @PostConstruct
    public void listenToChanges() {
        reactiveMongoTemplate.changeStream(ChatMessage.class)
                .watchCollection(ChatMessage.class)
                .listen()
                .doOnSubscribe(sub -> log.info("👂 ChangeStream 구독 시작됨"))
                .doOnEach(signal -> log.debug("🚨 ChangeStream Signal 발생: {}", signal))
                .filter(event -> {
                    if (event.getRaw() == null || event.getRaw().getOperationType() == null) return false;
                    String op = event.getRaw().getOperationType().getValue();
                    return "insert".equals(op) || "update".equals(op);
                })
                .filter(event -> {
                    String op = event.getRaw().getOperationType().getValue();
                    ChatMessage body = event.getBody();
                    if (body == null) return false;

                    boolean shouldEmit = "insert".equals(op) || ("update".equals(op) && body.isRead());
                    log.info("🔎 ChangeStream 필터 조건 검사: op={}, read={}, emitToSink={}", op, body.isRead(), shouldEmit);
                    return shouldEmit;
                })
                .map(ChangeStreamEvent::getBody)
                .filter(Objects::nonNull)
                .map(ResponseChatMessageDto::from)
                .doOnNext(dto -> {
                    log.info("📥 [ChangeStream] emitToSink to sink: chatRoomId={}, read={}", dto.getChatRoomId(), dto.isRead());
                    chatMessageService.emitToSink(dto.getChatRoomId(), dto);
                })
                .doOnError(error -> log.error("❌ ChangeStream 처리 중 에러 발생", error))
                .subscribe();
    }

}
