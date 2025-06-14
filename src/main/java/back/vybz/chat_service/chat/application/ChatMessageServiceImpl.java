package back.vybz.chat_service.chat.application;

import back.vybz.chat_service.chat.domain.ChatMessage;
import back.vybz.chat_service.chat.dto.request.RequestEnterChatRoomDto;
import back.vybz.chat_service.chat.dto.request.RequestSendMessageDto;
import back.vybz.chat_service.chat.dto.response.ResponseChatMessageDto;
import back.vybz.chat_service.chat.infrastructure.ChatMessageReactiveRepository;
import back.vybz.chat_service.chat.infrastructure.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageReactiveRepository chatMessageReactiveRepository;
    private final ChatRoomService chatRoomService;

    /**
     * 메시지 전송 (Reactive 저장 + 마지막 메시지 갱신)
     *
     * @param requestSendMessageDto
     */
    @Override
    public Mono<Void> sendMessage(RequestSendMessageDto requestSendMessageDto) {
        ChatMessage chatMessage = requestSendMessageDto.toDocument();
        return chatMessageReactiveRepository.save(chatMessage)
                .doOnSuccess(savedMessage -> {
                    chatRoomService.updateLastMessage(requestSendMessageDto.getChatRoomId(), savedMessage);
                    chatRoomService.increaseUnreadCount(requestSendMessageDto.getChatRoomId(), requestSendMessageDto.getSenderUuid());
                })
                .then();
    }

    /**
     * 채팅방 ID로 메시지 스트림 구독
     *
     * @param chatRoomId
     */
    @Override
    public Flux<ResponseChatMessageDto> subscribeChatMessageByChatRoomId(String chatRoomId, String participantUuid) {
        return chatMessageReactiveRepository.findChatMessageByChatRoomId(chatRoomId)
                .map(ResponseChatMessageDto::from);
    }

    /**
     * 채팅방 ID로 이전 메시지 조회
     *
     * @param chatRoomId
     */
    @Override
    public List<ResponseChatMessageDto> getPreviousChatMessageByChatRoomId(String chatRoomId) {
        return chatMessageRepository.findAllByChatRoomIdOrderBySentAtDesc(chatRoomId)
                .stream()
                .map(ResponseChatMessageDto::from)
                .toList();
    }

    /**
     * 채팅방 참여
     *
     * @param requestEnterChatRoomDto
     */
    @Override
    public Mono<Void> enterChatRoom(RequestEnterChatRoomDto requestEnterChatRoomDto) {
        return chatMessageReactiveRepository.findByChatRoomIdAndSenderUuidNotAndReadIsFalse(
                        requestEnterChatRoomDto.getChatRoomId(), requestEnterChatRoomDto.getParticipantUuid()
                )
                .flatMap(message -> {
                    message.markAsRead();
                    return chatMessageReactiveRepository.save(message);
                })
                .then(Mono.fromRunnable(() ->
                        chatRoomService.resetUnreadCount(requestEnterChatRoomDto.getChatRoomId(), requestEnterChatRoomDto.getParticipantUuid())));
    }

}
