package back.vybz.chat_service.chat.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageType {

    TEXT("텍스트"),
    IMAGE("이미지"),
    VIDEO("비디오");

    private final String description;

}
