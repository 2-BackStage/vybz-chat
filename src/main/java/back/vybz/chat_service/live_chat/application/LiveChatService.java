package back.vybz.chat_service.live_chat.application;

import back.vybz.chat_service.live_chat.dto.request.RequestSendLiveChatDto;

public interface LiveChatService {

    /**
     * 라이브 채팅 전송
     * @param requestSendLiveChatDto
     */
    void sendLiveChat(RequestSendLiveChatDto requestSendLiveChatDto);

}
