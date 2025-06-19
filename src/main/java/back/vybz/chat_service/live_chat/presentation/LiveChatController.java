package back.vybz.chat_service.live_chat.presentation;

import back.vybz.chat_service.live_chat.application.LiveChatService;
import back.vybz.chat_service.live_chat.dto.request.RequestSendLiveChatDto;
import back.vybz.chat_service.live_chat.vo.request.RequestSendLiveChatVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LiveChatController {

    private final LiveChatService liveChatService;

    @MessageMapping("/live-chat/sendMessage")
    public void sendLiveChat(@Payload RequestSendLiveChatVo requestSendLiveChatVo,
                             @Header("simpSessionAttributes")Map<String, Object> sessionAttributes) {
        String liveId = (String) sessionAttributes.get("liveId");
        if (liveId == null || liveId.isEmpty()) {
            log.warn("liveId not fount in session 메시지 무시됨: {}", requestSendLiveChatVo);
            return;
        }
        liveChatService.sendLiveChat(RequestSendLiveChatDto.from(liveId, requestSendLiveChatVo));
    }

}
