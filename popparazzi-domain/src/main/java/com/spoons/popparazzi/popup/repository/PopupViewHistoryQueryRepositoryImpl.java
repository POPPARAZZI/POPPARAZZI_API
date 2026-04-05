package com.spoons.popparazzi.popup.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spoons.popparazzi.popup.dto.query.PopupViewCountQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.spoons.popparazzi.popup.entity.QPopupViewHistory.popupViewHistory;

@Repository
@RequiredArgsConstructor
public class PopupViewHistoryQueryRepositoryImpl implements PopupViewHistoryQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public long countViews(String popupCode) {
        if (popupCode == null || popupCode.isBlank()) {
            return 0L;
        }

        Long count = queryFactory
                .select(popupViewHistory.count())
                .from(popupViewHistory)
                .where(popupViewHistory.popupCode.eq(popupCode))
                .fetchOne();

        return count == null ? 0L : count;
    }

/*    @Override
    public Map<String, Long> countViewsByPopupCodes(List<String> popupCodes) {
        if (popupCodes == null || popupCodes.isEmpty()) {
            return Collections.emptyMap();
        }

        return queryFactory
                .from(popupViewHistory)
                .where(popupViewHistory.popupCode.in(popupCodes))
                .transform(GroupBy.groupBy(popupViewHistory.popupCode).as(popupViewHistory.count()));
    }*/

    @Override
    public Map<String, Long> countViewsByPopupCodes(List<String> popupCodes) {
        if (popupCodes == null || popupCodes.isEmpty()) {
            return Collections.emptyMap();
        }

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

}