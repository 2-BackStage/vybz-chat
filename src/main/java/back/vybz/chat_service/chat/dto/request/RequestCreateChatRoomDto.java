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
    private boolean hidden;

    @Builder
    public RequestCreateChatRoomDto(String senderUuid, String receiverUuid, boolean hidden) {
        this.senderUuid = senderUuid;
        this.receiverUuid = receiverUuid;
        this.hidden = hidden;
    }

    public static RequestCreateChatRoomDto from(RequestCreateChatRoomVo requestCreateChatRoomVo) {
        return RequestCreateChatRoomDto.builder()
                .senderUuid(requestCreateChatRoomVo.getSenderUuid())
                .receiverUuid(requestCreateChatRoomVo.getReceiverUuid())
                .hidden(false) // 기본값으로 false 설정
                .build();
    }

    public ChatRoom toDocument() {
        return ChatRoom.builder()
                .participant(List.of(
                        new Participant(senderUuid, 0, hidden),
                        new Participant(receiverUuid, 0, hidden)
                ))
                .lastMessage(null)
                .build();
    }

}
