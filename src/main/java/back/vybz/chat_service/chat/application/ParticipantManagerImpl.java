package back.vybz.chat_service.chat.application;

import back.vybz.chat_service.common.util.ChatMessageChangeStreamListener;
import back.vybz.chat_service.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParticipantManagerImpl implements ParticipantManager {

    private final RedisUtil redisUtil;
    private final ChatMessageChangeStreamListener chatMessageChangeStreamListener;

    @Override
    public void registerParticipant(String chatRoomId, String participantUuid) {
        chatMessageChangeStreamListener.addParticipant(chatRoomId, participantUuid);
    }

    @Override
    public void unregisterParticipant(String chatRoomId, String participantUuid) {
        chatMessageChangeStreamListener.removeParticipant(chatRoomId, participantUuid);
    }
}
