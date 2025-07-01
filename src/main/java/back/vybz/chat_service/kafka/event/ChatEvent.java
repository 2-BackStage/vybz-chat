package back.vybz.chat_service.kafka.event;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatEvent {

    private String chatRoomId;
    private String senderUuid;
    private String receiverUuid;
    private String content;

    @Builder
    public ChatEvent(String chatRoomId, String senderUuid, String receiverUuid, String content) {
        this.chatRoomId = chatRoomId;
        this.senderUuid = senderUuid;
        this.receiverUuid = receiverUuid;
        this.content = content;
    }

}
