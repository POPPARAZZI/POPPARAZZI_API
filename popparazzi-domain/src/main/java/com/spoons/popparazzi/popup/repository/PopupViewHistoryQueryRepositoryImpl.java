package com.spoons.popparazzi.popup.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}