package back.vybz.chat_service.chat.presentation;

import back.vybz.chat_service.chat.application.ChatMessageService;
import back.vybz.chat_service.chat.dto.request.RequestEnterChatRoomDto;
import back.vybz.chat_service.chat.dto.request.RequestSendMessageDto;
import back.vybz.chat_service.chat.dto.response.ResponseChatMessageDto;
import back.vybz.chat_service.chat.vo.request.RequestEnterChatRoomVo;
import back.vybz.chat_service.chat.vo.request.RequestSendMessageVo;
import back.vybz.chat_service.chat.vo.response.ResponseChatMessageVo;
import back.vybz.chat_service.common.entity.BaseResponseEntity;
import back.vybz.chat_service.common.entity.BaseResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat-message")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    /**
     * 메시지 전송
     * @param requestSendMessageVo
     */
    @Operation(summary = "메시지 전송 API", description = "메시지 전송 API 입니다.", tags = {"Chat-Message-Service"})
    @PostMapping
    public BaseResponseEntity<Void> sendMessage(@RequestBody RequestSendMessageVo requestSendMessageVo) {
        chatMessageService.sendMessage(RequestSendMessageDto.from(requestSendMessageVo)).subscribe();
        return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS);
    }

    /**
     * 채팅방 ID로 실시간 메시지 구독
     * @param chatRoomId
     * @param participantUuid
     */
    @Operation(summary = "실시간 채팅방 메시지 구독 API", description = "실시간 채팅방 메시지 구독 API 입니다.", tags = {"Chat-Message-Service"})
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ResponseChatMessageVo> subscribeChatMessage(@RequestParam("chatRoomId") String chatRoomId, @RequestParam("participantUuid") String participantUuid) {
        return chatMessageService.subscribeChatMessageByChatRoomId(chatRoomId, participantUuid)
                .map(ResponseChatMessageDto::toVo);
    }

    /**
     * 채팅방 ID로 이전 메시지 조회
     * @param chatRoomId
     */
    // TODO : 커서 기반 페이징 처리로 전환 예정
    @Operation(summary = "채팅방 ID로 이전 메시지 조회 API", description = "채팅방 ID로 이전 메시지 조회 API 입니다.", tags = {"Chat-Message-Service"})
    @GetMapping("/{chatRoomId}")
    public BaseResponseEntity<List<ResponseChatMessageVo>> getPreviousChatMessage(@PathVariable("chatRoomId") String chatRoomId) {
        List<ResponseChatMessageVo> responseChatMessageVo = chatMessageService.getPreviousChatMessageByChatRoomId(chatRoomId)
                .stream()
                .map(ResponseChatMessageDto::toVo)
                .toList();
        return new BaseResponseEntity<>(responseChatMessageVo);
    }

    /**
     * 채팅방 참여
     * @param requestEnterChatRoomVo
     */
    @Operation(summary = "채팅방 참여 API", description = "채팅방 참여 API 입니다.", tags = {"Chat-Message-Service"})
    @PutMapping
    public BaseResponseEntity<Void> enterChatRoom(@RequestBody RequestEnterChatRoomVo requestEnterChatRoomVo) {
        chatMessageService.enterChatRoom(RequestEnterChatRoomDto.from(requestEnterChatRoomVo))
                .subscribe();
        return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS);
    }

}
