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

    private final Map<String, Sinks.Many<ResponseChatMessageDto>> sinkMap = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> roomParticipants = new ConcurrentHashMap<>();

    public Sinks.Many<ResponseChatMessageDto> getOrCreateSink(String chatRoomId) {
        return sinkMap.computeIfAbsent(chatRoomId, key -> {
            log.info("🛠️ 새로운 Sink 생성: chatRoomId={}", chatRoomId);
            return Sinks.many().multicast().onBackpressureBuffer();
        });
    }

    public void addParticipant(String chatRoomId, String participantUuid) {
        roomParticipants.computeIfAbsent(chatRoomId, key -> ConcurrentHashMap.newKeySet())
                .add(participantUuid);
        log.info("👥 참가자 등록: chatRoomId={}, participantUuid={}", chatRoomId, participantUuid);
    }

    public void removeParticipant(String chatRoomId, String participantUuid) {
        Set<String> participants = roomParticipants.get(chatRoomId);
        if (participants != null) {
            participants.remove(participantUuid);
            log.info("👋 참가자 퇴장: chatRoomId={}, participantUuid={}", chatRoomId, participantUuid);

            if (participants.isEmpty()) {
                roomParticipants.remove(chatRoomId);
                sinkMap.remove(chatRoomId);
                log.info("🧹 Sink 및 참가자 정보 제거: chatRoomId={}", chatRoomId);
            }
        }
    }

    public void emitToSink(String chatRoomId, ResponseChatMessageDto message) {
        Sinks.Many<ResponseChatMessageDto> sink = sinkMap.get(chatRoomId);
        if (sink != null) {
            Sinks.EmitResult result = sink.tryEmitNext(message);
            if (result.isFailure()) {
                log.warn("❌ 메시지 푸시 실패: {}, 이유: {}", message, result.name());
            } else {
                log.debug("📤 메시지 푸시 성공: chatRoomId={}, content={}", chatRoomId, message.getContent());
            }
        } else {
            log.warn("🚫 Sink 없음: chatRoomId={}", chatRoomId);
        }
    }
}
