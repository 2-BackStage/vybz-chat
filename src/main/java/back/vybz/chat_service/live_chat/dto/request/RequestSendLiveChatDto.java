package back.vybz.chat_service.live_chat.dto.request;

import back.vybz.chat_service.kafka.event.LiveChatEvent;
import back.vybz.chat_service.live_chat.vo.request.RequestSendLiveChatVo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestSendLiveChatDto {

    private String liveId;
    private String senderUuid;
    private String content;

    @Builder
    public RequestSendLiveChatDto(String liveId, String senderUuid, String content) {
        this.liveId = liveId;
        this.senderUuid = senderUuid;
        this.content = content;
    }

    public static RequestSendLiveChatDto from(String liveId, RequestSendLiveChatVo requestSendLiveChatVo) {
        return RequestSendLiveChatDto.builder()
                .liveId(liveId)
                .senderUuid(requestSendLiveChatVo.getSenderUuid())
                .content(requestSendLiveChatVo.getContent())
                .build();
    }

    public static LiveChatEvent toLiveChatEvent(String liveId, String senderUuid, String content) {
        return LiveChatEvent.builder()
                .liveId(liveId)
                .senderUuid(senderUuid)
                .content(content)
                .build();
    }

}
