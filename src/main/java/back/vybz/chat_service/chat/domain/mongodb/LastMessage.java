package back.vybz.chat_service.chat.domain.mongodb;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Getter
@NoArgsConstructor
@Document
public class LastMessage {

    //보낸내용
    @Field(name = "content")
    private String content;

    //보낸사람 UUid
    @Field(name = "sender_uuid")
    private String senderUuid;

    //보낸사람 타입
    @Field(name = "sender_type")
    private SenderType senderType;

    //보낸시간
    @Field(name = "send_at")
    private Instant sentAt;


}
