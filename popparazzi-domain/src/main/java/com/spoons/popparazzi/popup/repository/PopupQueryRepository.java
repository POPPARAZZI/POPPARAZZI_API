package com.spoons.popparazzi.popup.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spoons.popparazzi.popup.dto.query.PopupSearchMatchQuery;
import com.spoons.popparazzi.popup.dto.query.PopupSelectionItemQuery;
import com.spoons.popparazzi.popup.dto.query.PopupViewCountQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.spoons.popparazzi.popup.entity.QPopup.popup;
import static com.spoons.popparazzi.popup.entity.QPopupViewHistory.popupViewHistory;

@Repository
@RequiredArgsConstructor
public class PopupQueryRepository {

    private final JPAQueryFactory queryFactory;

    // ─────────────────────────────────────────────────────────────────────────
    // 검색
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @methodName  : findBestMatch
     * @author      : seulgi Yang
     * @param       : normalizedKeyword
     * @returnType  : Optional<PopupSearchMatchQuery>
     * @desc        : 검색어와 가장 일치하는 팝업 단건 조회.
     *               완전 일치 → 시작 일치 → 포함 일치 우선순위 정렬. 종료된 팝업 제외
     */
    public Optional<PopupSearchMatchQuery> findBestMatch(String normalizedKeyword) {
        if (normalizedKeyword == null || normalizedKeyword.isBlank()) {
            return Optional.empty();
        }

        PopupSearchMatchQuery result = queryFactory
                .select(Projections.constructor(
                        PopupSearchMatchQuery.class,
                        popup.popupCode,
                        popup.title,
                        popupAddress(),
                        popup.startDt,
                        popup.endDt
                ))
                .from(popup)
                .where(
                        titleContains(normalizedKeyword),
                        popup.endDt.after(LocalDateTime.now())
                )
                .orderBy(
                        matchPriority(normalizedKeyword),
                        popup.startDt.desc()
                )
                .fetchFirst();

        return Optional.ofNullable(result);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 선택 (모임 생성)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @methodName  : findNewestSelections
     * @author      : seulgi Yang
     * @param       : limit
     * @returnType  : List<PopupSelectionItemQuery>
     * @desc        : 모임 생성 시 팝업 선택 목록 조회. 종료되지 않은 팝업만 포함. 등록일 최신순
     */
    public List<PopupSelectionItemQuery> findNewestSelections(int limit) {
        return queryFactory
                .select(Projections.constructor(
                        PopupSelectionItemQuery.class,
                        popup.popupCode,
                        popup.title,
                        popup.addrSido,
                        popup.addrSigungu,
                        popup.startDt,
                        popup.endDt
                ))
                .from(popup)
                .where(popup.endDt.goe(LocalDateTime.now()))
                .orderBy(popup.regDt.desc())
                .limit(limit)
                .fetch();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 조회수
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @methodName  : countViews
     * @author      : seulgi Yang
     * @param       : popupCode
     * @returnType  : long
     * @desc        : 단일 팝업 조회수 집계
     */
    public long countViews(String popupCode) {
        if (popupCode == null || popupCode.isBlank()) return 0L;

        Long count = queryFactory
                .select(popupViewHistory.count())
                .from(popupViewHistory)
                .where(popupViewHistory.popupCode.eq(popupCode))
                .fetchOne();

        return count == null ? 0L : count;
    }

    /**
     * @methodName  : countViewsByPopupCodes
     * @author      : seulgi Yang
     * @param       : popupCodes
     * @returnType  : Map<String, Long>
     * @desc        : 여러 팝업 조회수 bulk 집계. popupCode → viewCount 맵 반환
     */
    public Map<String, Long> countViewsByPopupCodes(List<String> popupCodes) {
        if (popupCodes == null || popupCodes.isEmpty()) return Collections.emptyMap();

        List<PopupViewCountQuery> rows = queryFactory
                .select(Projections.constructor(
                        PopupViewCountQuery.class,
                        popupViewHistory.popupCode,
                        popupViewHistory.count()
                ))
                .from(popupViewHistory)
                .where(popupViewHistory.popupCode.in(popupCodes))
                .groupBy(popupViewHistory.popupCode)
                .fetch();

        return rows.stream()
                .collect(Collectors.toMap(
                        PopupViewCountQuery::popupCode,
                        PopupViewCountQuery::viewCount
                ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 내부 헬퍼
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * 우선순위: 0 완전 일치 → 1 시작 일치 → 2 포함 일치
     */
    private OrderSpecifier<Integer> matchPriority(String normalizedKeyword) {
        NumberExpression<Integer> priority = new CaseBuilder()
                .when(normalizedTitle().eq(normalizedKeyword)).then(0)
                .when(normalizedTitle().startsWith(normalizedKeyword)).then(1)
                .otherwise(2);

        return priority.asc();
    }

    private BooleanExpression titleContains(String normalizedKeyword) {
        return normalizedTitle().like("%" + normalizedKeyword + "%");
    }

    private StringExpression normalizedTitle() {
        return Expressions.stringTemplate("replace({0}, ' ', '')", popup.title);
    }

    private StringExpression popupAddress() {
        return Expressions.stringTemplate("concat({0}, ' ', {1})", popup.addrSido, popup.addrSigungu);
    }
}
