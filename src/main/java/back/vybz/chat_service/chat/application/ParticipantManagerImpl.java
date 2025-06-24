package back.vybz.chat_service.chat.application;

import back.vybz.chat_service.common.util.ChatSinkManager;
import back.vybz.chat_service.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParticipantManagerImpl implements ParticipantManager {

    private final RedisUtil redisUtil;
    private final ChatSinkManager chatSinkManager;

    @Override
    public void registerParticipant(String chatRoomId, String participantUuid) {
        redisUtil.addParticipantToChatRoom(chatRoomId, participantUuid);
        chatSinkManager.addParticipant(chatRoomId, participantUuid);
    }

    @Override
    public void unregisterParticipant(String chatRoomId, String participantUuid) {
        redisUtil.removeParticipantFromChatRoom(chatRoomId, participantUuid);
        chatSinkManager.removeParticipant(chatRoomId, participantUuid);
    }
}
