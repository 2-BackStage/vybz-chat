package back.vybz.chat_service.common.util;

import back.vybz.chat_service.chat.domain.ChatMessage;
import back.vybz.chat_service.chat.dto.response.ResponseChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageChangeStreamListener {

    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final ChatMessageChangeFilter chatMessageChangeFilter;
    private final RedisUtil redisUtil;

    // 채팅방별 참가자 관리
    private final Map<String, Set<String>> roomParticipants = new ConcurrentHashMap<>();
    
    // 채팅방별 Sink (읽음 처리된 메시지 전송용)
    private final Map<String, Sinks.Many<ResponseChatMessageDto>> roomSinks = new ConcurrentHashMap<>();

    /**
     * 채팅방에 참가자 등록
     */
    public void addParticipant(String chatRoomId, String participantUuid) {
        roomParticipants.computeIfAbsent(chatRoomId, id -> ConcurrentHashMap.newKeySet())
                .add(participantUuid);
        redisUtil.addParticipantToChatRoom(chatRoomId, participantUuid);
        log.info("👥 참가자 등록: chatRoomId={}, participantUuid={}", chatRoomId, participantUuid);
    }

    /**
     * 채팅방에서 참가자 제거
     */
    public void removeParticipant(String chatRoomId, String participantUuid) {
        try {
            log.info("🔄 참가자 제거 시작: chatRoomId={}, participantUuid={}", chatRoomId, participantUuid);
            
            // 메모리에서 참가자 제거
            Set<String> participants = roomParticipants.get(chatRoomId);
            if (participants != null) {
                boolean removed = participants.remove(participantUuid);
                log.info("👋 메모리에서 참가자 제거: chatRoomId={}, participantUuid={}, removed={}", 
                    chatRoomId, participantUuid, removed);

                if (participants.isEmpty()) {
                    cleanupChatRoom(chatRoomId);
                }
            } else {
                log.warn("⚠️ 메모리에 참가자 정보 없음: chatRoomId={}, participantUuid={}", chatRoomId, participantUuid);
            }
            
            // Redis에서 참가자 제거
            redisUtil.removeParticipantFromChatRoom(chatRoomId, participantUuid);
            log.info("✅ 참가자 제거 완료: chatRoomId={}, participantUuid={}", chatRoomId, participantUuid);
            
        } catch (Exception e) {
            log.error("❌ 참가자 제거 중 에러 발생: chatRoomId={}, participantUuid={}, error={}", 
                chatRoomId, participantUuid, e.getMessage(), e);
        }
    }

    /**
     * 특정 채팅방의 Change Stream 구독
     */
    public Flux<ResponseChatMessageDto> subscribeToChatRoom(String chatRoomId) {
        return reactiveMongoTemplate.changeStream(ChatMessage.class)
                .watchCollection(ChatMessage.class)
                .listen()
                // insert 또는 update 이벤트인지 필터링
                .filter(chatMessageChangeFilter::isRelevantOperation)
                // insert는 무조건, update는 read=true일 때만 emit 허용
                .filter(chatMessageChangeFilter::shouldEmit)
                // 실제 메시지 데이터(body)만 추출
                .map(ChangeStreamEvent::getBody)
                .filter(Objects::nonNull)
                // 특정 채팅방의 메시지만 필터링
                .filter(message -> chatRoomId.equals(message.getChatRoomId()))
                // 메시지를 클라이언트로 보낼 DTO로 변환
                .map(ResponseChatMessageDto::from)
                .doOnError(error -> log.error("❌ ChangeStream 처리 중 에러 발생: chatRoomId={}", chatRoomId, error));
    }

    /**
     * 채팅방 Sink 생성 또는 가져오기
     */
    public Sinks.Many<ResponseChatMessageDto> getOrCreateRoomSink(String chatRoomId) {
        return roomSinks.computeIfAbsent(chatRoomId, key -> {
            log.info("🛠️ 새로운 Room Sink 생성: chatRoomId={}", chatRoomId);
            return Sinks.many().multicast().onBackpressureBuffer(5000);
        });
    }

    /**
     * 채팅방 정리
     */
    private void cleanupChatRoom(String chatRoomId) {
        roomParticipants.remove(chatRoomId);
        roomSinks.remove(chatRoomId);
        log.info("🧹 채팅방 정리 완료: chatRoomId={}", chatRoomId);
    }

    /**
     * 읽음 처리된 메시지를 Sink를 통해 직접 전송
     */
    public void emitReadMessages(String chatRoomId, List<ResponseChatMessageDto> readMessages) {
        if (readMessages.isEmpty()) {
            log.debug("📖 읽음 처리된 메시지 없음: chatRoomId={}", chatRoomId);
            return;
        }
        
        log.info("📖 읽음 처리된 메시지 {}개 즉시 전송: chatRoomId={}", readMessages.size(), chatRoomId);
        
        Sinks.Many<ResponseChatMessageDto> sink = getOrCreateRoomSink(chatRoomId);
        
        // 각 읽음 처리된 메시지를 Sink를 통해 전송
        readMessages.forEach(readMessage -> {
            Sinks.EmitResult result = sink.tryEmitNext(readMessage);
            if (result.isSuccess()) {
                log.info("📤 읽음 메시지 전송 성공: chatRoomId={}, messageId={}, read={}", 
                    chatRoomId, readMessage.getId(), readMessage.isRead());
            } else {
                log.warn("❌ 읽음 메시지 전송 실패: chatRoomId={}, messageId={}, reason={}", 
                    chatRoomId, readMessage.getId(), result.name());
            }
        });
    }
}
