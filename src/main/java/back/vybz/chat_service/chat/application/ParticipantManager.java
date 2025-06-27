package back.vybz.chat_service.chat.application;

public interface ParticipantManager {

    void registerParticipant(String chatRoomId, String participantUuid);

    void unregisterParticipant(String chatRoomId, String participantUuid);

}
