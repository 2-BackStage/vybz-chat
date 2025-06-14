package back.vybz.chat_service.chat.presentation;

import back.vybz.chat_service.chat.application.ChatRoomService;
import back.vybz.chat_service.chat.dto.request.RequestCreateChatRoomDto;
import back.vybz.chat_service.chat.dto.response.ResponseChatRoomDto;
import back.vybz.chat_service.chat.vo.request.RequestCreateChatRoomVo;
import back.vybz.chat_service.chat.vo.response.ResponseChatRoomVo;
import back.vybz.chat_service.common.entity.BaseResponseEntity;
import back.vybz.chat_service.common.entity.BaseResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat-room")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @Operation(summary = "채팅방 생성/재참여 API" , description = "채팅방 생성/재참여 API 입니다.", tags = {"Chat-Room-Service"})
    @PostMapping
    public BaseResponseEntity<Void> createChatRoom(@RequestBody RequestCreateChatRoomVo requestCreateChatRoomVo) {
        chatRoomService.createChatRoom(RequestCreateChatRoomDto.from(requestCreateChatRoomVo));
        return new BaseResponseEntity<>(BaseResponseStatus.SUCCESS);
    }

    @Operation(summary = "사용자 UUID로 채팅방 조회 API", description = "사용자 UUID로 채팅방 조회 API 입니다.", tags = {"Chat-Room-Service"})
    @GetMapping("/{participantUuid}")
    public BaseResponseEntity<List<ResponseChatRoomVo>> getChatRoomByParticipantUuid(@PathVariable("participantUuid") String participantUuid) {
        List<ResponseChatRoomVo> responseChatRoomVo = chatRoomService.getChatRoomByParticipantUuid(participantUuid)
                .stream()
                .map(ResponseChatRoomDto::toVo)
                .toList();
        return new BaseResponseEntity<>(responseChatRoomVo);
    }

}
