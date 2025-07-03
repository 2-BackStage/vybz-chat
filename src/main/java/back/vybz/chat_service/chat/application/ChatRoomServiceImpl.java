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
        try {
            // 입력값 검증
            if (requestCreateChatRoomDto.getSenderUuid() == null || requestCreateChatRoomDto.getSenderUuid().trim().isEmpty()) {
                throw new BaseException(BaseResponseStatus.INVALID_INPUT_VALUE);
            }
            if (requestCreateChatRoomDto.getReceiverUuid() == null || requestCreateChatRoomDto.getReceiverUuid().trim().isEmpty()) {
                throw new BaseException(BaseResponseStatus.INVALID_INPUT_VALUE);
            }
            if (requestCreateChatRoomDto.getSenderUuid().equals(requestCreateChatRoomDto.getReceiverUuid())) {
                throw new BaseException(BaseResponseStatus.INVALID_INPUT_VALUE);
            }

            // 모든 채팅방을 먼저 가져와서 Java에서 필터링
            List<ChatRoom> allChatRooms = chatRoomRepository.findAllChatRoomByTwoParticipants(
                    requestCreateChatRoomDto.getReceiverUuid(),
                    requestCreateChatRoomDto.getSenderUuid()
            );

            log.info("🔍 모든 채팅방 검색 결과: {}개", allChatRooms.size());
            for (ChatRoom room : allChatRooms) {
                log.info("  - 채팅방 ID: {}, 참여자 상태: {}", 
                    room.getId(), 
                    room.getParticipant().stream()
                        .map(p -> p.getParticipantUuid() + ":" + (p.isHidden() ? "hidden" : "active"))
                        .toList()
                );
            }

            // 활성화된 채팅방 찾기 (두 사용자가 모두 hidden=false인 방)
            List<ChatRoom> activeChatRooms = allChatRooms.stream()
                    .filter(room -> {
                        boolean senderActive = room.getParticipant().stream()
                                .anyMatch(p -> p.getParticipantUuid().equals(requestCreateChatRoomDto.getSenderUuid()) && !p.isHidden());
                        boolean receiverActive = room.getParticipant().stream()
                                .anyMatch(p -> p.getParticipantUuid().equals(requestCreateChatRoomDto.getReceiverUuid()) && !p.isHidden());
                        return senderActive && receiverActive;
                    })
                    .toList();

            log.info("🔍 활성화된 채팅방 필터링 결과: {}개", activeChatRooms.size());

            // 활성화된 채팅방이 있으면 반환
            if (!activeChatRooms.isEmpty()) {
                log.info("✅ 기존 활성화된 채팅방 발견: {}", activeChatRooms.get(0).getId());
                return activeChatRooms.get(0);
            }

            // 기존 채팅방들 중에서 재활성화할 수 있는 방 찾기
            for (ChatRoom chatRoom : allChatRooms) {
                boolean allHidden = chatRoom.getParticipant().stream()
                        .allMatch(Participant::isHidden);

                if (allHidden) {
                    // 모든 참여자가 hidden 상태인 경우 → 새로운 채팅방 생성
                    log.info("🔍 모든 참여자가 hidden 상태인 채팅방 발견: {}", chatRoom.getId());
                    continue;
                } else {
                    // 일부 참여자가 활성 상태인 경우 → 이 채팅방을 재활성화
                    log.info("🔄 일부 참여자가 활성 상태인 채팅방 발견: {}", chatRoom.getId());
                    
                    // 현재 요청한 사용자들을 재활성화
                    chatRoom.getParticipant().forEach(p -> {
                        if (p.getParticipantUuid().equals(requestCreateChatRoomDto.getSenderUuid()) ||
                            p.getParticipantUuid().equals(requestCreateChatRoomDto.getReceiverUuid())) {
                            p.rejoin();
                        }
                    });
                    
                    ChatRoom saved = chatRoomRepository.save(chatRoom);
                    log.info("🔄 기존 채팅방 재활성화 완료: {}", saved.getId());
                    return saved;
                }
            }

            // 모든 방이 hidden 상태이거나 존재하지 않는 경우 → 새로 생성
            ChatRoom chatRoom = requestCreateChatRoomDto.toDocument();
            log.info("📌 새 채팅방 생성 시작: senderUuid={}, receiverUuid={}", 
                    requestCreateChatRoomDto.getSenderUuid(), requestCreateChatRoomDto.getReceiverUuid());
            ChatRoom saved = chatRoomRepository.save(chatRoom);
            log.info("✅ 새 채팅방 생성 완료: {}", saved.getId());
            return saved;
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ 채팅방 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new BaseException(BaseResponseStatus.INTERNAL_SERVER_ERROR);
        }
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
