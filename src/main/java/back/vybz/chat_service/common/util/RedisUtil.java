package back.vybz.chat_service.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate stringRedisTemplate;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    private static final String CHAT_ROOM_PARTICIPANT_PREFIX = "chat:room:";

    /**
     * 사용자 채팅방 입장 처리
     */
    public void addParticipantToChatRoom(String chatRoomId, String participantUuid) {
        String key = getKey(chatRoomId);
        try {
            Long addedCount = stringRedisTemplate.opsForSet().add(key, participantUuid);
            log.info("👤 Redis 참가자 추가됨: chatRoomId={}, participantUuid={}, addedCount={}, key={}", 
                chatRoomId, participantUuid, addedCount, key);
        } catch (Exception e) {
            log.error("❌ Redis 참가자 추가 실패: chatRoomId={}, participantUuid={}, error={}", 
                chatRoomId, participantUuid, e.getMessage(), e);
        }
    }

    /**
     * 사용자 채팅방 퇴장 처리 (키 삭제)
     */
    public void removeParticipantFromChatRoom(String chatRoomId, String participantUuid) {
        String key = getKey(chatRoomId);
        try {
            Long removedCount = stringRedisTemplate.opsForSet().remove(key, participantUuid);
            log.info("👋 Redis 참가자 제거됨: chatRoomId={}, participantUuid={}, removedCount={}, key={}", 
                chatRoomId, participantUuid, removedCount, key);
            
            Long remainingMembers = stringRedisTemplate.opsForSet().size(key);
            log.info("📊 Redis 남은 참가자 수: chatRoomId={}, remainingMembers={}", chatRoomId, remainingMembers);

            // 남은 멤버가 없으면 키 자체를 삭제
            if (remainingMembers != null && remainingMembers == 0) {
                Boolean keyDeleted = stringRedisTemplate.delete(key);
                log.info("🗑️ Redis 키 삭제: chatRoomId={}, key={}, deleted={}", chatRoomId, key, keyDeleted);
            }
        } catch (Exception e) {
            log.error("❌ Redis 참가자 제거 실패: chatRoomId={}, participantUuid={}, error={}", 
                chatRoomId, participantUuid, e.getMessage(), e);
        }
    }

    /**
     * 사용자가 현재 채팅방에 접속 중인지 확인
     */
    public Mono<Boolean> isParticipantOnline(String chatRoomId, String participantUuid) {
        if (chatRoomId == null || participantUuid == null) {
            log.warn("⚠️ chatRoomId 또는 participantUuid가 null입니다. chatRoomId={}, participantUuid={}", chatRoomId, participantUuid);
            return Mono.just(false);
        }

        String key = getKey(chatRoomId);
        return reactiveRedisTemplate.opsForSet()
                .isMember(key, participantUuid)
                .timeout(Duration.ofSeconds(2))
                .onErrorResume(e -> Mono.just(false))
                .defaultIfEmpty(false);
    }

    private String getKey(String chatRoomId) {
        return CHAT_ROOM_PARTICIPANT_PREFIX + chatRoomId + ":participants:";
    }
}