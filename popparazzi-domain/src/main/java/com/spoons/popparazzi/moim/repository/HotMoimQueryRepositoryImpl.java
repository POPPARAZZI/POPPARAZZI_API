package com.spoons.popparazzi.moim.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spoons.popparazzi.like.entity.LikeType;
import com.spoons.popparazzi.moim.dto.query.HotMoimCardQuery;
import com.spoons.popparazzi.moim.dto.query.HotMoimRankQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.spoons.popparazzi.like.entity.QLikeMapping.likeMapping;
import static com.spoons.popparazzi.moim.entity.QMoim.moim;
import static com.spoons.popparazzi.moim.entity.QMoimMemberMapping.moimMemberMapping;

@Repository
@RequiredArgsConstructor
public class HotMoimQueryRepositoryImpl implements HotMoimQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 24시간(또는 since 이후) 좋아요 기준 핫 모임 랭킹 Top N
     * - 반환: (moimCode, likeCount24h)
     */
    @Override
    public List<HotMoimRankQuery> findHotRankKeys(LikeType type, LocalDateTime since, Pageable pageable) {

        var likeCount = likeMapping.count();

        return queryFactory
                .select(Projections.constructor(
                        HotMoimRankQuery.class,
                        likeMapping.targetCode,
                        likeCount
                ))
                .from(likeMapping)
                .where(
                        likeMapping.type.eq(type),
                        likeMapping.createdAt.goe(since)
                )
                .groupBy(likeMapping.targetCode)
                .orderBy(likeCount.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    /**
     * 핫한 모임 카드 베이스(참여자수, 기본정보) 조립
     */
    @Override
    public List<HotMoimCardQuery> findHotCardsBase(List<String> mmCodes) {
        if (mmCodes == null || mmCodes.isEmpty()) return List.of();

        var joinedCount = new CaseBuilder()
                .when(moimMemberMapping.joinYn.eq("Y")).then(1)
                .otherwise(0)
                .sum()
                .intValue(); // Integer expression으로 변환

        return queryFactory
                .select(Projections.constructor(
                        HotMoimCardQuery.class,
                        moim.moimCode,
                        moim.popupCode,
                        moim.title,
                        moim.date,
                        joinedCount,           // currentParticipants
                        moim.maxParticipants,
                        com.querydsl.core.types.dsl.Expressions.nullExpression(String.class), // thumbnailUrl (나중에 붙임)
                        com.querydsl.core.types.dsl.Expressions.constant(0L)                  // likeCount24h (나중에 ranks에서 붙임)
                ))
                .from(moim)
                .leftJoin(moimMemberMapping)
                .on(moimMemberMapping.id.moimCode.eq(moim.moimCode))
                .where(
                        moim.moimCode.in(mmCodes),
                        moim.deleteYn.eq("N")
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