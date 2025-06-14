package back.vybz.chat_service.chat.vo.request;

import back.vybz.chat_service.chat.domain.MessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestSendMessageVo {

    private String chatRoomId;
    private String senderUuid;
    private MessageType messageType;
    private String content;

}
