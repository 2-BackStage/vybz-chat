package back.vybz.chat_service.chat.application;

import back.vybz.chat_service.chat.domain.ChatMessage;
import back.vybz.chat_service.chat.domain.ChatRoom;
import back.vybz.chat_service.chat.dto.request.RequestCreateChatRoomDto;
import back.vybz.chat_service.chat.dto.request.RequestLeaveChatRoomDto;
import back.vybz.chat_service.chat.dto.response.ResponseChatRoomDto;
import back.vybz.chat_service.common.util.CursorPageUtil;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

public interface ChatRoomService {

    /**
     * 채팅방 생성
     * @param requestCreateChatRoomDto
     */
    Mono<ChatRoom> createChatRoom(RequestCreateChatRoomDto requestCreateChatRoomDto);

    /**
     * 참여자 UUID로 채팅방 조회
     * @param participantUuid
     */
    Mono<CursorPageUtil<ResponseChatRoomDto, Instant>> getChatRoomByParticipantUuidWithCursor(String participantUuid, Instant sentAt, Integer pageSize);

    /**
     * 특정 채팅방에 참여자 UUID가 활성화 상태인지 확인
     * @param room
     * @param participantUuid
     */
    boolean isParticipantActive(ChatRoom room, String participantUuid);

    /**
     * 마지막 메시지 업데이트
     * @param chatRoomId
     * @param chatMessage
     */
    Mono<Void> updateLastMessage(String chatRoomId, ChatMessage chatMessage);

    /**
     * 읽지않은 메시지 수 증가
     * @param chatRoomId
     * @param senderUuid
     */
    Mono<Void> increaseUnreadCount(String chatRoomId, String senderUuid);

    /**
     * 읽지않은 메시지 수 초기화
     * @param chatRoomId
     * @param participantUuid
     */
    Mono<Void> resetUnreadCount(String chatRoomId, String participantUuid);

    /**
     * 채팅방 나가기
     * @param requestLeaveChatRoomDto
     */
    Mono<Void> leaveChatRoom(RequestLeaveChatRoomDto requestLeaveChatRoomDto);

    /**
     * 채팅방 재참여
     * @param chatRoomId
     * @param participantUuid
     */
    Mono<Void> rejoinIfHidden(String chatRoomId, List<String> participantUuid);

}
