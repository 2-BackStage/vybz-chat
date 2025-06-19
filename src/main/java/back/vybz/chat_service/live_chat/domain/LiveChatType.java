package back.vybz.chat_service.live_chat.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LiveChatType {

    JOIN("참여자 입장"),
    CHAT("채팅 메시지"),
    LEAVE("참여자 나감"),
    CLOSE("라이브 종료");

    private final String description;

}
