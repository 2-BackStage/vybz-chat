package back.vybz.chat_service.common.util;

import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@NoArgsConstructor
public class MongoCursorHelper {

    /**
     * 커서 기반 + 오프셋 기반 페이징 쿼리 구성
     * @param query         초기 Criteria 조건이 담긴 Query
     * @param cursorField   커서로 사용할 필드 (예: "lastMessage.sentAt")
     * @param cursorValue   커서 값 (예: Instant 등 Comparable 타입)
     * @param pageSize      페이지 크기
     * @param sortDirection 정렬 방향
     * @return 페이징이 적용된 Query
     */
    public static <T extends Comparable<T>> Query build(Query query, String cursorField, T cursorValue, int pageSize, Sort.Direction sortDirection) {
        if (cursorValue != null) {
            if (sortDirection == Sort.Direction.DESC) {
                query.addCriteria(Criteria.where(cursorField).lt(cursorValue));
            } else {
                query.addCriteria(Criteria.where(cursorField).gt(cursorValue));
            }
        }

        query.with(Sort.by(sortDirection, cursorField));
        query.limit(pageSize + 1); // hasNext 판별용

        return query;
    }

}
