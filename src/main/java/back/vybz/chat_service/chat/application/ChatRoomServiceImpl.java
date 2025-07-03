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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
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
        log.info("🚀 채팅방 생성 시작: senderUuid={}, receiverUuid={}", 
                requestCreateChatRoomDto.getSenderUuid(), requestCreateChatRoomDto.getReceiverUuid());
        
        // 단일 쿼리로 모든 채팅방 조회 후 Java에서 처리 (성능 최적화)
        List<ChatRoom> allChatRooms = chatRoomRepository.findAllChatRoomByTwoParticipants(
                requestCreateChatRoomDto.getReceiverUuid(),
                requestCreateChatRoomDto.getSenderUuid()
        );

        log.info("🔍 기존 채팅방 검색 결과: {}개", allChatRooms.size());

        // 기존 채팅방이 없는 경우 → 새로운 채팅방 생성
        if (allChatRooms.isEmpty()) {
            log.info("📌 기존 채팅방 없음 → 새로운 채팅방 생성");
            ChatRoom chatRoom = requestCreateChatRoomDto.toDocument();
            ChatRoom saved = chatRoomRepository.save(chatRoom);
            log.info("✅ 새 채팅방 생성 완료: {}", saved.getId());
            return saved;
        }

        // Java에서 채팅방 상태 분석 및 처리
        ChatRoom activeChatRoom = null;
        ChatRoom reactivableChatRoom = null;

        for (ChatRoom chatRoom : allChatRooms) {
            boolean senderActive = chatRoom.getParticipant().stream()
                    .anyMatch(p -> p.getParticipantUuid().equals(requestCreateChatRoomDto.getSenderUuid()) && !p.isHidden());
            boolean receiverActive = chatRoom.getParticipant().stream()
                    .anyMatch(p -> p.getParticipantUuid().equals(requestCreateChatRoomDto.getReceiverUuid()) && !p.isHidden());

            // 둘 다 활성 상태인 경우
            if (senderActive && receiverActive) {
                activeChatRoom = chatRoom;
                break; // 최적의 경우 발견, 즉시 종료
            }
            // 둘 중 하나라도 활성 상태인 경우 (재활성화 가능)
            else if (senderActive || receiverActive) {
                reactivableChatRoom = chatRoom;
            }
        }

        // 활성화된 채팅방이 있으면 반환
        if (activeChatRoom != null) {
            log.info("✅ 기존 활성화된 채팅방 발견: {}", activeChatRoom.getId());
            return activeChatRoom;
        }

        // 재활성화 가능한 채팅방이 있으면 재활성화
        if (reactivableChatRoom != null) {
            log.info("🔄 기존 채팅방 재활성화: {}", reactivableChatRoom.getId());
            
            reactivableChatRoom.getParticipant().forEach(p -> {
                if (p.getParticipantUuid().equals(requestCreateChatRoomDto.getSenderUuid()) ||
                    p.getParticipantUuid().equals(requestCreateChatRoomDto.getReceiverUuid())) {
                    p.rejoin();
                }
            });
            
            ChatRoom saved = chatRoomRepository.save(reactivableChatRoom);
            log.info("✅ 채팅방 재활성화 완료: {}", saved.getId());
            return saved;
        }

        // 모든 기존 채팅방이 hidden 상태인 경우 → 새로운 채팅방 생성
        log.info("📌 모든 기존 채팅방이 hidden 상태 → 새로운 채팅방 생성");
        ChatRoom chatRoom = requestCreateChatRoomDto.toDocument();
        ChatRoom saved = chatRoomRepository.save(chatRoom);
        log.info("✅ 새 채팅방 생성 완료: {}", saved.getId());
        return saved;
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
