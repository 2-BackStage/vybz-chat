package back.vybz.chat_service.live_chat.application;

import back.vybz.chat_service.kafka.producer.LiveChatKafkaProducer;
import back.vybz.chat_service.live_chat.dto.request.RequestSendLiveChatDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveChatServiceImpl implements LiveChatService {

    private final LiveChatKafkaProducer liveChatKafkaProducer;

    /**
     * 라이브 채팅 전송
     * @param requestSendLiveChatDto
     */
    @Override
    public void sendLiveChat(RequestSendLiveChatDto requestSendLiveChatDto) {
        liveChatKafkaProducer.sendChatMessage(RequestSendLiveChatDto.toLiveChatEvent(
                requestSendLiveChatDto.getLiveId(), requestSendLiveChatDto.getSenderUuid(), requestSendLiveChatDto.getContent()));
        log.info("라이브 채팅 전송 완료: {}, {}, {}", requestSendLiveChatDto.getLiveId(), requestSendLiveChatDto.getSenderUuid(), requestSendLiveChatDto.getContent());
    }
}
