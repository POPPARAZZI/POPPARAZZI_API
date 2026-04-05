package com.spoons.popparazzi.like.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spoons.popparazzi.like.dto.query.LikeCountQuery;
import com.spoons.popparazzi.like.dto.query.LikeRankQuery;
import com.spoons.popparazzi.like.enums.LikeType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.spoons.popparazzi.like.entity.QLikeMapping.likeMapping;

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

        return queryFactory
                .select(Projections.constructor(
                        LikeRankQuery.class,
                        likeMapping.targetCode,
                        likeMapping.count()
                ))
                .from(likeMapping)
                .where(
                        likeMapping.type.eq(type),
                        likeMapping.createdAt.goe(since)
                )
                .groupBy(likeMapping.targetCode)
                .orderBy(likeMapping.count().desc())
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
        if (type == null || targetCodes == null || targetCodes.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .select(likeMapping.targetCode)
                .from(likeMapping)
                .where(
                        likeMapping.memberCode.eq(memberCode),
                        likeMapping.type.eq(type),
                        likeMapping.targetCode.in(targetCodes)
                )
                .fetch();
    }

    @Override
    public Map<String, Long> countTargetsByType(LikeType type, List<String> targetCodes) {
        if (type == null || targetCodes == null || targetCodes.isEmpty()) {
            return Collections.emptyMap();
        }

        List<LikeCountQuery> rows = queryFactory
                .select(Projections.constructor(
                        LikeCountQuery.class,
                        likeMapping.targetCode,
                        likeMapping.count()
                ))
                .from(likeMapping)
                .where(
                        likeMapping.type.eq(type),
                        likeMapping.targetCode.in(targetCodes)
                )
                .groupBy(likeMapping.targetCode)
                .fetch();

        return rows.stream()
                .collect(Collectors.toMap(
                        LikeCountQuery::targetCode,
                        LikeCountQuery::likeCount
                ));
    }
}