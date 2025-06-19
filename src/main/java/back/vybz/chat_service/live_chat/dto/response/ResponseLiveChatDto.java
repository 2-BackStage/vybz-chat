package back.vybz.chat_service.live_chat.dto.response;

import back.vybz.chat_service.kafka.event.LiveChatEvent;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ResponseLiveChatDto {

    private String senderUuid;
    private String content;

    @Builder
    public ResponseLiveChatDto(String senderUuid, String content) {
        this.senderUuid = senderUuid;
        this.content = content;
    }

    public static ResponseLiveChatDto from(LiveChatEvent liveChatEvent) {
        return ResponseLiveChatDto.builder()
                .senderUuid(liveChatEvent.getSenderUuid())
                .content(liveChatEvent.getContent())
                .build();
    }

}
