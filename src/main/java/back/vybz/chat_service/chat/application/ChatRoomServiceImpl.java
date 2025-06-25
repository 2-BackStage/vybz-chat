package back.vybz.chat_service.chat.application;

import back.vybz.chat_service.chat.domain.ChatMessage;
import back.vybz.chat_service.chat.domain.ChatRoom;
import back.vybz.chat_service.chat.domain.Participant;
import back.vybz.chat_service.chat.dto.request.RequestCreateChatRoomDto;
import back.vybz.chat_service.chat.dto.request.RequestLeaveChatRoomDto;
import back.vybz.chat_service.chat.dto.response.ResponseChatRoomDto;
import back.vybz.chat_service.chat.infrastructure.ChatRoomRepository;
import back.vybz.chat_service.common.entity.BaseResponseStatus;
import back.vybz.chat_service.common.exception.BaseException;
import back.vybz.chat_service.common.util.CursorPageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    /**
     * 채팅방 생성
     * @param requestCreateChatRoomDto
     */
    @Override
    public ChatRoom createChatRoom(RequestCreateChatRoomDto requestCreateChatRoomDto) {
        Optional<ChatRoom> existChatRoom = chatRoomRepository.findChatRoomByTwoParticipants(
                requestCreateChatRoomDto.getReceiverUuid(), requestCreateChatRoomDto.getSenderUuid());
        if (existChatRoom.isPresent()) {
            ChatRoom chatRoom = existChatRoom.get();
            boolean allHidden = chatRoom.getParticipant().stream()
                    .allMatch(Participant::isHidden);
            if (!allHidden) {
                return chatRoom;
            }
        }
        ChatRoom chatRoom = requestCreateChatRoomDto.toDocument();
        return chatRoomRepository.save(chatRoom);
    }

    /**
     * 참여자 UUID로 채팅방 목록 조회
     * @param participantUuid
     */
    @Override
    public CursorPageUtil<ResponseChatRoomDto, Instant> getChatRoomByParticipantUuidWithCursor(String participantUuid, Instant sentAt, Integer pageSize) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByParticipantUuidWithCursor(participantUuid, sentAt, pageSize);
        boolean hasNext = chatRooms.size() > pageSize;
        if (hasNext) {
            chatRooms = chatRooms.subList(0, pageSize);
        }
        List<ResponseChatRoomDto> responseChatRoomDto = chatRooms.stream()
                .map(ResponseChatRoomDto::from)
                .toList();
        Instant nextCursor = hasNext ? chatRooms.get(pageSize - 1).getLastMessage().getSentAt() : null;
        return CursorPageUtil.<ResponseChatRoomDto, Instant>builder()
                .content(responseChatRoomDto)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .pageSize(pageSize)
                .build();
    }

    /**
     * 마지막 메시지 업데이트
     * @param chatRoomId
     * @param chatMessage
     */
    @Override
    public void updateLastMessage(String chatRoomId, ChatMessage chatMessage) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_CHAT_ROOM));
        chatRoom.updateLastMessage(chatMessage);
        chatRoomRepository.save(chatRoom);
    }

    /**
     * 읽지않은 메시지 수 증가
     * @param chatRoomId
     * @param senderUuid
     */
    @Override
    public void increaseUnreadCount(String chatRoomId, String senderUuid) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_CHAT_ROOM));
        chatRoom.increaseUnreadCountExcept(senderUuid);
        chatRoomRepository.save(chatRoom);
    }

    /**
     * 읽지않은 메시지 수 초기화
     * @param chatRoomId
     * @param participantUuid
     */
    @Override
    public void resetUnreadCount(String chatRoomId, String participantUuid) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_CHAT_ROOM));
        chatRoom.resetUnreadCount(participantUuid);
        chatRoomRepository.save(chatRoom);
    }

    /**
     * 채팅방 나가기
     * @param requestLeaveChatRoomDto
     */
    @Override
    public void leaveChatRoom(RequestLeaveChatRoomDto requestLeaveChatRoomDto) {
        ChatRoom chatRoom = chatRoomRepository.findById(requestLeaveChatRoomDto.getChatRoomId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_CHAT_ROOM));
        chatRoom.getParticipant().stream()
                .filter(p -> p.getParticipantUuid().equals(requestLeaveChatRoomDto.getParticipantUuid()))
                .findFirst()
                .ifPresent(participant -> {
                    participant.leave();
                    participant.resetUnreadCount();
                });
        chatRoomRepository.save(chatRoom);
    }

    /**
     * 채팅방 재참여
     * @param chatRoomId
     * @param participantUuid
     */
    @Override
    public void rejoinIfHidden(String chatRoomId, List<String> participantUuid) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NO_EXIST_CHAT_ROOM));

        chatRoom.getParticipant().forEach(p -> {
            if (participantUuid.contains(p.getParticipantUuid())) {
                p.rejoin();
            }
        });

        chatRoomRepository.save(chatRoom);
    }

}
