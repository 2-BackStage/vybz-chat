package back.vybz.chat_service.chat.dto.request;

import back.vybz.chat_service.chat.domain.ChatRoom;
import back.vybz.chat_service.chat.domain.Participant;
import back.vybz.chat_service.chat.vo.request.RequestCreateChatRoomVo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RequestCreateChatRoomDto {

    private String senderUuid;
    private String receiverUuid;

    @Builder
    public RequestCreateChatRoomDto(String senderUuid, String receiverUuid) {
        this.senderUuid = senderUuid;
        this.receiverUuid = receiverUuid;
    }

    public static RequestCreateChatRoomDto from(RequestCreateChatRoomVo requestCreateChatRoomVo) {
        return RequestCreateChatRoomDto.builder()
                .senderUuid(requestCreateChatRoomVo.getSenderUuid())
                .receiverUuid(requestCreateChatRoomVo.getReceiverUuid())
                .build();
    }

    public ChatRoom toDocument() {
        return ChatRoom.builder()
                .participant(List.of(
                        new Participant(senderUuid, 0),
                        new Participant(receiverUuid, 0)
                ))
                .lastMessage(null)
                .build();
    }

}
