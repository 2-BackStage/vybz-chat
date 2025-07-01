package back.vybz.chat_service.chat.dto.request;

import back.vybz.chat_service.chat.domain.ChatMessage;
import back.vybz.chat_service.chat.domain.MessageType;
import back.vybz.chat_service.chat.vo.request.RequestSendMessageVo;
import back.vybz.chat_service.kafka.event.ChatEvent;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
public class RequestSendMessageDto {

    private String chatRoomId;
    private String senderUuid;
    private String receiverUuid;
    private MessageType messageType;
    private String content;

    @Builder
    public RequestSendMessageDto(String chatRoomId, String senderUuid, String receiverUuid, MessageType messageType, String content) {
        this.chatRoomId = chatRoomId;
        this.senderUuid = senderUuid;
        this.receiverUuid = receiverUuid;
        this.messageType = messageType;
        this.content = content;
    }

    public static RequestSendMessageDto from(RequestSendMessageVo requestSendMessageVo) {
        return RequestSendMessageDto.builder()
                .chatRoomId(requestSendMessageVo.getChatRoomId())
                .senderUuid(requestSendMessageVo.getSenderUuid())
                .receiverUuid(requestSendMessageVo.getReceiverUuid())
                .messageType(requestSendMessageVo.getMessageType())
                .content(requestSendMessageVo.getContent())
                .build();
    }

    public ChatMessage toDocument() {
        return ChatMessage.builder()
                .chatRoomId(chatRoomId)
                .senderUuid(senderUuid)
                .receiverUuid(receiverUuid)
                .messageType(messageType)
                .content(content)
                .sentAt(Instant.now())
                .build();
    }

    public ChatEvent toChatEvent() {
        return ChatEvent.builder()
                .chatRoomId(chatRoomId)
                .senderUuid(senderUuid)
                .receiverUuid(receiverUuid)
                .content(content)
                .build();
    }

}
