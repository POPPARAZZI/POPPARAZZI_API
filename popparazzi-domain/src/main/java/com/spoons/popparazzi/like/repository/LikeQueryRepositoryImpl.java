package com.spoons.popparazzi.like.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spoons.popparazzi.like.dto.query.LikeRankQuery;
import com.spoons.popparazzi.like.entity.QLikeMapping;
import com.spoons.popparazzi.like.enums.LikeType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class LikeQueryRepositoryImpl implements LikeQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<LikeRankQuery> findTopRankKeys(
            LikeType type,
            LocalDateTime since,
            Pageable pageable
    ) {
        if (type == null || since == null || pageable == null) {
            return List.of();
        }

        QLikeMapping like = QLikeMapping.likeMapping;

        return queryFactory
                .select(Projections.constructor(
                        LikeRankQuery.class,
                        like.targetCode,
                        like.count()
                ))
                .from(like)
                .where(
                        like.type.eq(type),
                        like.createdAt.goe(since)
                )
                .groupBy(like.targetCode)
                .orderBy(like.count().desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<String> findLikedTargetCodes(
            String memberCode,
            LikeType type,
            List<String> targetCodes
    ) {
        if (memberCode == null || type == null || targetCodes == null || targetCodes.isEmpty()) {
            return List.of();
        }

        QLikeMapping like = QLikeMapping.likeMapping;

        return queryFactory
                .select(like.targetCode)
                .from(like)
                .where(
                        like.memberCode.eq(memberCode),
                        like.type.eq(type),
                        like.targetCode.in(targetCodes)
                )
                .fetch();
    }
}