package com.spoons.popparazzi.moim.repository.hot;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spoons.popparazzi.common.YesNo;
import com.spoons.popparazzi.moim.dto.result.HotMoimCardResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.spoons.popparazzi.moim.entity.QMoim.moim;
import static com.spoons.popparazzi.moim.entity.QMoimMemberMapping.moimMemberMapping;

@Repository
@RequiredArgsConstructor
public class HotMoimQueryRepositoryImpl implements HotMoimQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<HotMoimCardResult> findHotCardsBase(List<String> mmCodes) {
        if (mmCodes == null || mmCodes.isEmpty()) return List.of();

        var joinedCount = new CaseBuilder()
                .when(moimMemberMapping.joinYn.eq(YesNo.YES)).then(1)
                .otherwise(0)
                .sum()
                .intValue();

        return queryFactory
                .select(Projections.constructor(
                        HotMoimCardResult.class,
                        moim.moimCode,
                        moim.popupCode,
                        moim.title,
                        moim.date,
                        joinedCount,
                        moim.maxParticipants,
                        com.querydsl.core.types.dsl.Expressions.nullExpression(String.class),
                        com.querydsl.core.types.dsl.Expressions.constant(0L)
                ))
                .from(moim)
                .leftJoin(moimMemberMapping)
                .on(moimMemberMapping.id.moimCode.eq(moim.moimCode))
                .where(
                        moim.moimCode.in(mmCodes),
                        moim.deleteYn.eq(YesNo.NO)
                )
                .groupBy(
                        moim.moimCode,
                        moim.popupCode,
                        moim.title,
                        moim.date,
                        moim.maxParticipants
                )
                .fetch();
    }
}