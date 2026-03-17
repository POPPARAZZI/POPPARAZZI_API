package com.spoons.popparazzi.popup.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spoons.popparazzi.popup.dto.query.PopupSearchMatchQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.spoons.popparazzi.popup.entity.QPopup.popup;

@Repository
@RequiredArgsConstructor
public class PopupSearchQueryRepositoryImpl implements PopupSearchQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
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
                .where(titleContains(normalizedKeyword),
                        popup.endDt.after(LocalDateTime.now()))
                .orderBy(
                        matchPriority(normalizedKeyword),
                        popup.startDt.desc()
                )
                .fetchFirst();

        return Optional.ofNullable(result);
    }

    private BooleanExpression titleContains(String normalizedKeyword) {
        return normalizedTitle().like("%" + normalizedKeyword + "%");
    }

    /**
     * 우선순위
     * 0: 완전 일치
     * 1: 시작 일치
     * 2: 포함 일치
     */
    private OrderSpecifier<Integer> matchPriority(String normalizedKeyword) {
        NumberExpression<Integer> priority = new CaseBuilder()
                .when(normalizedTitle().eq(normalizedKeyword)).then(0)
                .when(normalizedTitle().startsWith(normalizedKeyword)).then(1)
                .otherwise(2);

        return priority.asc();
    }

    private StringExpression normalizedTitle() {
        return Expressions.stringTemplate(
                "replace({0}, ' ', '')",
                popup.title
        );
    }

    private StringExpression popupAddress() {
        return Expressions.stringTemplate(
                "concat({0}, ' ', {1})",
                popup.addrSido,
                popup.addrSigungu
        );
    }
}