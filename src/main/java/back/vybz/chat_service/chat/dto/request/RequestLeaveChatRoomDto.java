package back.vybz.chat_service.chat.dto.request;

import back.vybz.chat_service.chat.domain.ChatMessage;
import back.vybz.chat_service.chat.domain.MessageType;
import back.vybz.chat_service.chat.vo.request.RequestLeaveChatRoomVo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
public class RequestLeaveChatRoomDto {

    private String chatRoomId;
    private String participantUuid;
    private boolean hidden;

    @Builder
    public RequestLeaveChatRoomDto(String chatRoomId, String participantUuid, boolean hidden) {
        this.chatRoomId = chatRoomId;
        this.participantUuid = participantUuid;
        this.hidden = hidden;
    }

    public ChatMessage toDocument() {
        return ChatMessage.builder()
                .chatRoomId(chatRoomId)
                .senderUuid(participantUuid)
                .messageType(MessageType.LEFT)
                .content(participantUuid + " 님이 채팅방을 나갔습니다.")
                .read(true)
                .sentAt(Instant.now())
                .build();
    }

    public static RequestLeaveChatRoomDto from(RequestLeaveChatRoomVo requestLeaveChatRoomVo) {
        return RequestLeaveChatRoomDto.builder()
                .chatRoomId(requestLeaveChatRoomVo.getChatRoomId())
                .participantUuid(requestLeaveChatRoomVo.getParticipantUuid())
                .build();
    }

    public static RequestLeaveChatRoomDto hiddenChatRoom(RequestLeaveChatRoomVo requestLeaveChatRoomVo) {
        return RequestLeaveChatRoomDto.builder()
                .chatRoomId(requestLeaveChatRoomVo.getChatRoomId())
                .participantUuid(requestLeaveChatRoomVo.getParticipantUuid())
                .hidden(true)
                .build();
    }

}
