package back.vybz.chat_service.chat.dto.request;

import back.vybz.chat_service.chat.vo.request.RequestEnterChatRoomVo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestEnterChatRoomDto {

    private String chatRoomId;
    private String participantUuid;

    @Builder
    public RequestEnterChatRoomDto(String chatRoomId, String participantUuid) {
        this.chatRoomId = chatRoomId;
        this.participantUuid = participantUuid;
    }

    public static RequestEnterChatRoomDto from(RequestEnterChatRoomVo requestEnterChatRoomVo) {
        return RequestEnterChatRoomDto.builder()
                .chatRoomId(requestEnterChatRoomVo.getChatRoomId())
                .participantUuid(requestEnterChatRoomVo.getParticipantUuid())
                .build();
    }


}
