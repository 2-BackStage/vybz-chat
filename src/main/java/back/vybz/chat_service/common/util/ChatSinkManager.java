package back.vybz.chat_service.common.util;

import back.vybz.chat_service.chat.dto.response.ResponseChatMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatSinkManager {

    private static final int SINK_BUFFER_SIZE = 5000;

    private final Map<String, Sinks.Many<ResponseChatMessageDto>> sinkMap = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> roomParticipants = new ConcurrentHashMap<>();

    /**
     * 채팅방 Sink 생성 또는 가져오기
     */
    public Sinks.Many<ResponseChatMessageDto> getOrCreateSink(String chatRoomId) {
        return sinkMap.computeIfAbsent(chatRoomId, key -> {
            log.info("🛠️ 새로운 Sink 생성: chatRoomId={}", chatRoomId);
            return Sinks.many().multicast().onBackpressureBuffer(SINK_BUFFER_SIZE);
        });
    }

    /**
     * 채팅방에 참가자 추가
     */
    public void addParticipant(String chatRoomId, String participantUuid) {
        roomParticipants.computeIfAbsent(chatRoomId, id -> ConcurrentHashMap.newKeySet())
                .add(participantUuid);
        log.info("👥 참가자 등록: chatRoomId={}, participantUuid={}", chatRoomId, participantUuid);
    }

    /**
     * 채팅방에서 참가자 제거 및 비어있을 경우 정리
     */
    public void removeParticipant(String chatRoomId, String participantUuid) {
        Set<String> participants = roomParticipants.get(chatRoomId);
        if (participants == null)
            return;

        participants.remove(participantUuid);
        log.info("👋 참가자 퇴장: chatRoomId={}, participantUuid={}", chatRoomId, participantUuid);

        if (participants.isEmpty()) {
            cleanupChatRoom(chatRoomId);
        }
    }

    /**
     * Sink로 메시지 emit 처리
     */
    public void emitToSink(String chatRoomId, ResponseChatMessageDto message) {
        emitToSinkWithRetry(chatRoomId, message, 0);
    }

    private void emitToSinkWithRetry(String chatRoomId, ResponseChatMessageDto message, int retryCount) {
        Sinks.Many<ResponseChatMessageDto> sink = sinkMap.get(chatRoomId);
        if (sink == null) {
            log.warn("🚫 Sink 없음: chatRoomId={}", chatRoomId);
            return;
        }
        Sinks.EmitResult result = sink.tryEmitNext(message);
        if (result.isFailure()) {
            log.warn("❌ 메시지 emit 실패: chatRoomId={}, reason={}, message={}, retry={}", chatRoomId, result.name(), message, retryCount);
            if (retryCount < 3) {
                try {
                    Thread.sleep(10 * (retryCount + 1)); // 간단한 backoff
                } catch (InterruptedException ignored) {}
                emitToSinkWithRetry(chatRoomId, message, retryCount + 1);
            }
        } else {
            log.debug("📤 메시지 emit 성공: chatRoomId={}, content={}", chatRoomId, message.getContent());
        }
    }

    /**
     * 참가자 및 Sink 정리
     */
    private void cleanupChatRoom(String chatRoomId) {
        roomParticipants.remove(chatRoomId);
        sinkMap.remove(chatRoomId);
        log.info("🧹 채팅방 정리 완료: chatRoomId={}", chatRoomId);
    }

}
