package back.vybz.chat_service.chat.application;

import back.vybz.chat_service.chat.dto.request.RequestEnterChatRoomDto;
import back.vybz.chat_service.chat.dto.request.RequestSendMessageDto;
import back.vybz.chat_service.chat.dto.response.ResponseChatMessageDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ChatMessageService {

    /**
     * 채팅 메시지 전송
     * @param requestSendMessageDto
     */
    Mono<Void> sendMessage(RequestSendMessageDto requestSendMessageDto);

    /**
     * 채팅 메시지 실시간 구독
     * @param chatRoomId
     */
    Flux<ResponseChatMessageDto> subscribeChatMessageByChatRoomId(String chatRoomId, String participantUuid);

    /**
     * 채팅방 ID로 이전 메시지 조회
     * @param chatRoomId
     */
    Mono<List<ResponseChatMessageDto>> getPreviousChatMessageByChatRoomId(String chatRoomId, String participantUuid);

    void emitToSink(String chatRoomId, ResponseChatMessageDto responseChatMessageDto);

    boolean isParticipantOnline(String chatRoomId, String participantUuid);

}
