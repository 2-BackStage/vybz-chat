package back.vybz.chat_service.chat.domain;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Participant {

    /**
     * 참여자 UUID
     */
    private String participantUuid;

    /**
     * 안 읽은 메시지 수
     */
    private Integer unreadCount;

    /**
     * 숨김 처리
     */
    @Builder.Default
    private boolean hidden = false;

    public void incrementUnreadCount() {
        this.unreadCount++;
    }

    public void resetUnreadCount() {
        this.unreadCount = 0;
    }

    public void leave() {
        this.hidden = true;
    }

    public void rejoin() {
        this.hidden = false;
    }

}
