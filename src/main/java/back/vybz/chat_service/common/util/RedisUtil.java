package back.vybz.chat_service.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String CHAT_ROOM_PARTICIPANT_PREFIX = "chat:room:";

    /**
     * 사용자 채팅방 입장 처리
     */
    public void addParticipantToChatRoom(String chatRoomId, String participantUuid) {
        String key = getKey(chatRoomId);
        stringRedisTemplate.opsForSet().add(key, participantUuid);
        log.info("👤 Redis 참가자 추가됨: chatRoomId={}, participantUuid={}", chatRoomId, participantUuid);
    }

    /**
     * 사용자 채팅방 퇴장 처리 (키 삭제)
     */
    public void removeParticipantFromChatRoom(String chatRoomId, String participantUuid) {
        String key = getKey(chatRoomId);
        stringRedisTemplate.opsForSet().remove(key, participantUuid);
        log.info("👋 Redis 참가자 제거됨: chatRoomId={}, participantUuid={}", chatRoomId, participantUuid);
    }

    /**
     * 사용자가 현재 채팅방에 접속 중인지 확인
     */
    public boolean isParticipantOnline(String chatRoomId, String participantUuid) {
        if (chatRoomId == null || participantUuid == null) {
            log.warn("⚠️ chatRoomId 또는 participantUuid가 null입니다. chatRoomId={}, participantUuid={}", chatRoomId, participantUuid);
            return false;
        }
        String key = getKey(chatRoomId);
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, participantUuid);
        log.info("🔍 Redis 참가자 조회: key={}, participantUuid={}, isOnline={}", key, participantUuid, isMember);
        return Boolean.TRUE.equals(isMember);
    }

    private String getKey(String chatRoomId) {
        return CHAT_ROOM_PARTICIPANT_PREFIX + chatRoomId + ":participants:";
    }
}