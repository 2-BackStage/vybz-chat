package back.vybz.chat_service.common.util;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Function;

@Getter
@NoArgsConstructor
public class CursorPageUtil<T, C> {

    private List<T> content;
    private C nextCursor; // 제네릭 커서 타입
    private Boolean hasNext;
    private Integer pageSize;

    public boolean hasNext() {
        return nextCursor != null;
    }

    @Builder
    public CursorPageUtil(
            List<T> content, // 현재 페이지 데이터 목록
            C nextCursor, // 다음 페이지 조회용 커서
            Boolean hasNext, // 다음 페이지 존재 여부
            Integer pageSize // 한 페이지당 데이터 개수
    ) {
        this.content = content;
        this.nextCursor = nextCursor;
        this.hasNext = hasNext;
        this.pageSize = pageSize;
    }

    /**
     * DTO → VO 등 변환용 헬퍼 메서드
     * @param mapper 변환 함수
     * @return 변환된 커서 페이지 객체
     * @param <R> 변환 대상 타입
     */
    public <R> CursorPageUtil<R, C> map(Function<T, R> mapper) {
        List<R> mappedContent = this.content.stream().map(mapper).toList();
        return new CursorPageUtil<>(mappedContent, nextCursor, hasNext, pageSize);
    }

}
