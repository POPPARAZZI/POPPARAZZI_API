package com.spoons.popparazzi.like.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spoons.popparazzi.like.entity.LikeType;
import com.spoons.popparazzi.like.entity.QLikeMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LikeQueryRepositoryImpl implements LikeQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<String> findLikedMoimCodes(String memberCode, List<String> moimCodes) {

        if (memberCode == null || moimCodes.isEmpty()) return List.of();

        QLikeMapping like = QLikeMapping.likeMapping;

        return queryFactory
                .select(like.targetCode)
                .from(like)
                .where(
                        like.memberCode.eq(memberCode),
                        like.type.eq(LikeType.M),
                        like.targetCode.in(moimCodes)
                )
                .fetch();
    }
}
