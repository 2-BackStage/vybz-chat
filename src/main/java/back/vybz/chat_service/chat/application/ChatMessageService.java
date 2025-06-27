package back.vybz.chat_service.chat.application;

import back.vybz.chat_service.chat.dto.request.RequestLeaveChatRoomDto;
import back.vybz.chat_service.chat.dto.request.RequestSendMessageDto;
import back.vybz.chat_service.chat.dto.response.ResponseChatMessageDto;
import back.vybz.chat_service.common.util.CursorPageUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
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
     * 채팅방 ID로 이전 메시지 조회(커서 기반)
     * @param chatRoomId
     */
    Mono<CursorPageUtil<ResponseChatMessageDto, Instant>> getPreviousChatMessageByChatRoomId(String chatRoomId, String participantUuid, Instant sentAt, Integer pageSize);

    /**
     * 싱크로 메시지 발행
     * @param chatRoomId
     * @param responseChatMessageDto
     */
    void emitToSink(String chatRoomId, ResponseChatMessageDto responseChatMessageDto);

    /**
     * 핑 메시지 발행
     * @param chatRoomId
     */
    Flux<ResponseChatMessageDto> makePingFlux(String chatRoomId);

    /**
     * 읽지 않은 메시지를 읽음으로 표시, 실시간 emit
     * @param chatRoomId
     * @param participantUuid
     */
    Mono<Void> markUnreadMessagesAsRead(String chatRoomId, String participantUuid);

    /**
     * 읽지않은 메시지 수 초기화
     * @param chatRoomId
     * @param participantUuid
     */
    Mono<Void> resetUnreadCount(String chatRoomId, String participantUuid);

    /**
     * 채팅방의 메시지 조회, 참여자 퇴장 고려(커서 기반)
     */
    Mono<List<ResponseChatMessageDto>> fetchMessagesConsideringLeaveWithCursor(String chatRoomId, String participantUuid, Instant sentAt, Integer pageSize);

    /**
     * 채팅방 나가기 메시지 발행
     * @param requestLeaveChatRoomDto
     */
    Mono<Void> leaveChatRoomMessage(RequestLeaveChatRoomDto requestLeaveChatRoomDto);

    /**
     * 시스템 메시지 전송
     * @param chatRoomId
     * @param content
     */
    Mono<Void> sendSystemMessage(String chatRoomId, String content);

}
