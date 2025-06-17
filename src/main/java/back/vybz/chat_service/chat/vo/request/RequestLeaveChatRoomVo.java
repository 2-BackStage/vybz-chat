package back.vybz.chat_service.chat.vo.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestLeaveChatRoomVo {

    private String chatRoomId;
    private String participantUuid;

}
