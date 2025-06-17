package back.vybz.chat_service.chat.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SenderType {

    USER("사용자"),
    BUSKER("버스커");

    private final String description;

}
