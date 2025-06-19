package back.vybz.chat_service.live_chat.vo.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestSendLiveChatVo {

    private String liveId;
    private String senderUuid;
    private String content;

    @Builder
    public RequestSendLiveChatVo(String liveId, String senderUuid, String content) {
        this.liveId = liveId;
        this.senderUuid = senderUuid;
        this.content = content;
    }

}
