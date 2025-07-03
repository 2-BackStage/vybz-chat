package back.vybz.chat_service.common.util;

import back.vybz.chat_service.chat.application.ChatMessageService;
import back.vybz.chat_service.chat.domain.ChatMessage;
import back.vybz.chat_service.chat.dto.response.ResponseChatMessageDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageChangeStreamListener {

    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final ChatMessageService chatMessageService;
    private final ChatMessageChangeFilter chatMessageChangeFilter;

    /**
     * 컴포넌트 초기화 시점에 ChangeStream 구독
     */
    @PostConstruct
    public void listenToChanges() {
        // ChatMessage Collection 대상으로 Flux<ChangeStreamEvent<ChatMessage>> 생성
        reactiveMongoTemplate.changeStream(ChatMessage.class)
                .watchCollection(ChatMessage.class)
                .listen()
                // insert 또는 update 이벤트인지 필터링
                .filter(chatMessageChangeFilter::isRelevantOperation)
                // insert는 무조건, update는 read=true일 때만 emit 허용
                .filter(chatMessageChangeFilter::shouldEmit)
                // 실제 메시지 데이터(body)만 추출
                .map(ChangeStreamEvent::getBody)
                .filter(Objects::nonNull)
                // 메시지를 클라이언트로 보낼 DTO로 변환
                .map(ResponseChatMessageDto::from)
                // Sink에 emit하여 SSE 구독자에게 전송
                .doOnNext(dto -> {chatMessageService.emitToSink(dto.getChatRoomId(), dto);
                })
                .doOnError(error -> log.error("❌ ChangeStream 처리 중 에러 발생", error))
                .subscribe();
    }

}
