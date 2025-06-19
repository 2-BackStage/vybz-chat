package back.vybz.chat_service.kafka.event;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LiveChatEvent {

    private String liveId;
    private String senderUuid;
    private String content;

    @Builder
    public LiveChatEvent(String liveId, String senderUuid, String content) {
        this.liveId = liveId;
        this.senderUuid = senderUuid;
        this.content = content;
    }

}
