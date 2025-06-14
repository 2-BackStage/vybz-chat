package back.vybz.chat_service.chat.dto.response;

import back.vybz.chat_service.chat.domain.ChatRoom;
import back.vybz.chat_service.chat.domain.LastMessage;
import back.vybz.chat_service.chat.domain.Participant;
import back.vybz.chat_service.chat.vo.response.ResponseChatRoomVo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class ResponseChatRoomDto {

    private String chatRoomId;
    private List<Participant> participant;
    private LastMessage lastMessage;

    @Builder
    public ResponseChatRoomDto(String chatRoomId, List<Participant> participant, LastMessage lastMessage) {
        this.chatRoomId = chatRoomId;
        this.participant = participant;
        this.lastMessage = lastMessage;
    }

    public static ResponseChatRoomDto from(ChatRoom chatRoom) {
        return ResponseChatRoomDto.builder()
                .chatRoomId(chatRoom.getId())
                .participant(chatRoom.getParticipant())
                .lastMessage(chatRoom.getLastMessage())
                .build();
    }

    public ResponseChatRoomVo toVo() {
        ZonedDateTime kstTime = this.lastMessage.getSentAt()
                .atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.of("Asia/Seoul"));

        return ResponseChatRoomVo.builder()
                .chatRoomId(chatRoomId)
                .participant(participant)
                .content(lastMessage.getContent())
                .messageType(lastMessage.getMessageType())
                .sentAt(kstTime.toLocalDateTime())
                .build();
    }

}
