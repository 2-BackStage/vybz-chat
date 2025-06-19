package back.vybz.chat_service.live_chat.presentation;

import back.vybz.chat_service.live_chat.dto.request.RequestSendLiveChatDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Live Chat WebSocket", description = "STOMP WebSocket 메시지 전송 설명용")
@RestController
@RequestMapping("/docs/live-chat")
public class LiveChatDocController {

    @Operation(summary = "💬 WebSocket 라이브 채팅 사용법 안내",
            description = """
               ✅ WebSocket 기반 라이브 채팅은 다음 경로로 연결합니다:

               - WebSocket 연결 주소: `ws://{도메인}/chat-service/ws/live-chat?liveId={라이브ID}`
               - STOMP 메시지 전송: `/app/live-chat/sendMessage`
               - STOMP 구독 경로: `/topic/live-chat/{liveId}`

               전송 메시지 예시:
               ```json
               {
                 "senderUuid": "user-uuid",
                 "content": "안녕하세요!"
               }
               ```
               메시지를 보내면 같은 liveId 방에 있는 사용자에게 브로드캐스트됩니다.
               """)
    @GetMapping("/docs/live-chat/websocket")
    public void showLiveChatSocketDocs() {
        // 설명용 엔드포인트이므로 실제 로직 없음
    }
}
