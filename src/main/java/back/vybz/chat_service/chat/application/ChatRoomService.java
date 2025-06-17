package back.vybz.chat_service.chat.application;

import back.vybz.chat_service.chat.domain.ChatMessage;
import back.vybz.chat_service.chat.dto.request.RequestCreateChatRoomDto;
import back.vybz.chat_service.chat.dto.request.RequestLeaveChatRoomDto;
import back.vybz.chat_service.chat.dto.response.ResponseChatRoomDto;
import back.vybz.chat_service.common.util.CursorPageUtil;

import java.time.Instant;
import java.util.List;

public interface ChatRoomService {

    /**
     * 채팅방 생성
     * @param requestCreateChatRoomDto
     */
    void createChatRoom(RequestCreateChatRoomDto requestCreateChatRoomDto);

    /**
     * 참여자 UUID로 채팅방 조회
     * @param participantUuid
     */
    CursorPageUtil<ResponseChatRoomDto, Instant> getChatRoomByParticipantUuidWithCursor(String participantUuid, Instant sentAt, Integer pageSize);

    /**
     * 마지막 메시지 업데이트
     * @param chatRoomId
     * @param chatMessage
     */
    void updateLastMessage(String chatRoomId, ChatMessage chatMessage);

    /**
     * 읽지않은 메시지 수 증가
     * @param chatRoomId
     * @param senderUuid
     */
    void increaseUnreadCount(String chatRoomId, String senderUuid);

    /**
     * 읽지않은 메시지 수 초기화
     * @param chatRoomId
     * @param participantUuid
     */
    void resetUnreadCount(String chatRoomId, String participantUuid);

    /**
     * 채팅방 나가기
     * @param requestLeaveChatRoomDto
     */
    void leaveChatRoom(RequestLeaveChatRoomDto requestLeaveChatRoomDto);

    /**
     * 채팅방 재참여
     * @param chatRoomId
     * @param participantUuid
     */
    void rejoinIfHidden(String chatRoomId, String participantUuid);

}
