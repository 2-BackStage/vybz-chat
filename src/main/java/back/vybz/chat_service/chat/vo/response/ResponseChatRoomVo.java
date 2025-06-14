package back.vybz.chat_service.chat.vo.response;

import back.vybz.chat_service.chat.domain.ChatRoom;
import back.vybz.chat_service.chat.domain.LastMessage;
import back.vybz.chat_service.chat.domain.MessageType;
import back.vybz.chat_service.chat.domain.Participant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseChatRoomVo {

    private String chatRoomId;
    private List<Participant> participant;
    private String content;
    private MessageType messageType;
    private LocalDateTime sentAt;

}
