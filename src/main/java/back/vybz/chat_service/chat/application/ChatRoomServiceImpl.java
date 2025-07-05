package back.vybz.chat_service.chat.application;

import back.vybz.chat_service.chat.domain.ChatMessage;
import back.vybz.chat_service.chat.domain.ChatRoom;
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
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
    public Mono<ChatRoom> createChatRoom(RequestCreateChatRoomDto requestCreateChatRoomDto) {
        return chatRoomRepository.findAllChatRoomByTwoParticipants(
                requestCreateChatRoomDto.getReceiverUuid(), requestCreateChatRoomDto.getSenderUuid())
                .collectList()
                .flatMap(chatRooms -> {
                    // 이미 둘 다 활성화된 채팅방이 있다면 반환
                    Optional<ChatRoom> activeRoom = chatRooms.stream()
                            .filter(room -> isParticipantActive(room, requestCreateChatRoomDto.getSenderUuid())
                                    && isParticipantActive(room, requestCreateChatRoomDto.getReceiverUuid()))
                            .findFirst();
                    if (activeRoom.isPresent()) {
                        return Mono.just(activeRoom.get());
                    }
                    
                    // 하나만 활성화된 채팅방이 있다면 → 재활성화
                    Optional<ChatRoom> reactivableRoom = chatRooms.stream()
                            .filter(room -> isParticipantActive(room, requestCreateChatRoomDto.getSenderUuid())
                                    || isParticipantActive(room, requestCreateChatRoomDto.getReceiverUuid()))
                            .findFirst();
                    if (reactivableRoom.isPresent()) {
                        ChatRoom room = reactivableRoom.get();
                        room.getParticipant().forEach(p -> {
                            if (p.getParticipantUuid().equals(requestCreateChatRoomDto.getSenderUuid()) ||
                                    p.getParticipantUuid().equals(requestCreateChatRoomDto.getReceiverUuid())) {
                                p.rejoin();
                            }
                        });
                        return chatRoomRepository.save(room);
                    }
                    
                    // 모든 기존 채팅방이 hidden 상태라면 → 새로 생성
                    return chatRoomRepository.save(requestCreateChatRoomDto.toDocument());
                });
    }

    /**
     * 참여자 UUID로 채팅방 목록 조회
     * @param participantUuid
     */
    @Override
    public Mono<CursorPageUtil<ResponseChatRoomDto, Instant>> getChatRoomByParticipantUuidWithCursor(String participantUuid, Instant sentAt, Integer pageSize) {
        return chatRoomRepository.findByParticipantUuidWithCursor(participantUuid, sentAt, pageSize)
                .collectList()
                .map(chatRooms -> {
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
                });
    }

    /**
     * 특정 채팅방에 참여자가 활성화되어 있는지 확인
     * @param room
     * @param participantUuid
     */
    @Override
    public boolean isParticipantActive(ChatRoom room, String participantUuid) {
        return room.getParticipant().stream()
                .anyMatch(p -> p.getParticipantUuid().equals(participantUuid) && !p.isHidden());
    }

    /**
     * 마지막 메시지 업데이트
     * @param chatRoomId
     * @param chatMessage
     */
    @Override
    public Mono<Void> updateLastMessage(String chatRoomId, ChatMessage chatMessage) {
        return chatRoomRepository.findById(chatRoomId)
                .switchIfEmpty(Mono.error(new BaseException(BaseResponseStatus.NO_EXIST_CHAT_ROOM)))
                .flatMap(chatRoom -> {
                    chatRoom.updateLastMessage(chatMessage);
                    return chatRoomRepository.save(chatRoom);
                })
                .then();
    }

    /**
     * 읽지않은 메시지 수 증가
     * @param chatRoomId
     * @param senderUuid
     */
    @Override
    public Mono<Void> increaseUnreadCount(String chatRoomId, String senderUuid) {
        return chatRoomRepository.findById(chatRoomId)
                .switchIfEmpty(Mono.error(new BaseException(BaseResponseStatus.NO_EXIST_CHAT_ROOM)))
                .flatMap(chatRoom -> {
                    chatRoom.increaseUnreadCountExcept(senderUuid);
                    return chatRoomRepository.save(chatRoom);
                })
                .then();
    }

    /**
     * 읽지않은 메시지 수 초기화
     * @param chatRoomId
     * @param participantUuid
     */
    @Override
    public Mono<Void> resetUnreadCount(String chatRoomId, String participantUuid) {
        return chatRoomRepository.findById(chatRoomId)
                .switchIfEmpty(Mono.error(new BaseException(BaseResponseStatus.NO_EXIST_CHAT_ROOM)))
                .flatMap(chatRoom -> {
                    chatRoom.resetUnreadCount(participantUuid);
                    return chatRoomRepository.save(chatRoom);
                })
                .then();
    }

    /**
     * 채팅방 나가기
     * @param requestLeaveChatRoomDto
     */
    @Override
    public Mono<Void> leaveChatRoom(RequestLeaveChatRoomDto requestLeaveChatRoomDto) {
        return chatRoomRepository.findById(requestLeaveChatRoomDto.getChatRoomId())
                .switchIfEmpty(Mono.error(new BaseException(BaseResponseStatus.NO_EXIST_CHAT_ROOM)))
                .flatMap(chatRoom -> {
                    chatRoom.getParticipant().stream()
                            .filter(p -> p.getParticipantUuid().equals(requestLeaveChatRoomDto.getParticipantUuid()))
                            .findFirst()
                            .ifPresent(participant -> {
                                participant.leave();
                                participant.resetUnreadCount();
                            });
                    return chatRoomRepository.save(chatRoom);
                })
                .then();
    }

    /**
     * 채팅방 재참여
     * @param chatRoomId
     * @param participantUuid
     */
    @Override
    public Mono<Void> rejoinIfHidden(String chatRoomId, List<String> participantUuid) {
        return chatRoomRepository.findById(chatRoomId)
                .switchIfEmpty(Mono.error(new BaseException(BaseResponseStatus.NO_EXIST_CHAT_ROOM)))
                .flatMap(chatRoom -> {
                    chatRoom.getParticipant().forEach(p -> {
                        if (participantUuid.contains(p.getParticipantUuid())) {
                            p.rejoin();
                        }
                    });
                    return chatRoomRepository.save(chatRoom);
                })
                .then();
    }

}
