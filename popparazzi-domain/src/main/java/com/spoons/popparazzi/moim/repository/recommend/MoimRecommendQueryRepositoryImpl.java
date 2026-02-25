package com.spoons.popparazzi.moim.repository.recommend;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spoons.popparazzi.category.enums.CategoryType;
import com.spoons.popparazzi.common.YesNo;
import com.spoons.popparazzi.like.enums.LikeType;
import com.spoons.popparazzi.moim.dto.query.recommend.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.spoons.popparazzi.category.entity.QCategoryMapping.categoryMapping;
import static com.spoons.popparazzi.category.entity.QCategoryMaster.categoryMaster;
import static com.spoons.popparazzi.like.entity.QLikeMapping.likeMapping;
import static com.spoons.popparazzi.moim.entity.QMoim.moim;
import static com.spoons.popparazzi.popup.entity.QPopup.popup;
import static com.spoons.popparazzi.moim.entity.QMoimMemberMapping.moimMemberMapping;

@Repository
@RequiredArgsConstructor
public class MoimRecommendQueryRepositoryImpl implements MoimRecommendQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 선호 시군구 TopK
     */
    @Override
    public List<PreferredSigunguQuery> findPreferredSigunguTop(String memberCode, int days, int topK) {

        LocalDateTime from = LocalDateTime.now().minusDays(days);

        return queryFactory
                .select(Projections.constructor(
                        PreferredSigunguQuery.class,
                        popup.addrSigungu,
                        likeMapping.id.count()
                ))
                .from(likeMapping)
                .join(moim).on(moim.moimCode.eq(likeMapping.targetCode))
                .join(popup).on(popup.popupCode.eq(moim.popupCode))
                .where(
                        likeMapping.memberCode.eq(memberCode),
                        likeMapping.type.eq(LikeType.M),
                        likeMapping.createdAt.goe(from),
                        moim.deleteYn.eq(YesNo.NO)
                )
                .groupBy(popup.addrSigungu)
                .orderBy(likeMapping.id.count().desc())
                .limit(topK)
                .fetch();
    }

    /**
     * 1차 추천 후보 조회
     */
    @Override
    public List<RecommendedMoimBaseQuery> findRecommendMoimCandidates(
            List<String> sigunguPriority,
            String memberCode,
            int limit
    ) {

        LocalDateTime fromDate = LocalDateTime.now().plusHours(24);

        return queryFactory
                .select(Projections.constructor(
                        RecommendedMoimBaseQuery.class,
                        moim.moimCode,
                        moim.popupCode,
                        popup.addrSigungu,
                        moim.title,
                        moim.date,
                        moim.maxParticipants,
                        moim.regDt
                ))
                .from(moim)
                .join(popup).on(popup.popupCode.eq(moim.popupCode))
                .where(
                        moim.deleteYn.eq(YesNo.NO),
                        moim.date.goe(fromDate),
                        popup.addrSigungu.in(sigunguPriority),

                        moim.moimCode.notIn(
                                queryFactory
                                        .select(likeMapping.targetCode)
                                        .from(likeMapping)
                                        .where(
                                                likeMapping.memberCode.eq(memberCode),
                                                likeMapping.type.eq(LikeType.M)
                                        )
                        )
                )
                .orderBy(
                        moim.date.asc(),
                        moim.regDt.desc()
                )
                .limit(limit)
                .fetch();
    }

    /**
     * 유저 선호 카테고리 TopN
     */
    @Override
    public List<PreferredCategoryQuery> findPreferredCategories(
            String memberCode,
            int days,
            int topN
    ) {

        LocalDateTime from = LocalDateTime.now().minusDays(days);

        return queryFactory
                .select(Projections.constructor(
                        PreferredCategoryQuery.class,
                        categoryMaster.code,
                        categoryMapping.categoryCode.count()
                ))
                .from(likeMapping)
                .join(moim).on(moim.moimCode.eq(likeMapping.targetCode))
                .join(categoryMapping).on(categoryMapping.parentCode.eq(moim.moimCode))
                .join(categoryMaster).on(categoryMaster.code.eq(categoryMapping.categoryCode))
                .where(
                        likeMapping.memberCode.eq(memberCode),
                        likeMapping.type.eq(LikeType.M),
                        likeMapping.createdAt.goe(from),
                        categoryMaster.type.eq(CategoryType.M),
                        moim.deleteYn.eq(YesNo.NO)
                )
                .groupBy(categoryMaster.code)
                .orderBy(categoryMapping.categoryCode.count().desc())
                .limit(topN)
                .fetch();
    }

    /**
     * 후보 모임들의 카테고리 조회
     */
    @Override
    public List<MoimCategoryLinkQuery> findMoimCategories(List<String> moimCodes) {

        return queryFactory
                .select(Projections.constructor(
                        MoimCategoryLinkQuery.class,
                        categoryMapping.parentCode,
                        categoryMapping.categoryCode
                ))
                .from(categoryMapping)
                .join(categoryMaster).on(categoryMaster.code.eq(categoryMapping.categoryCode))
                .where(
                        categoryMapping.parentCode.in(moimCodes),
                        categoryMaster.type.eq(CategoryType.M)
                )
                .fetch();
    }

    @Override
    public List<MoimParticipantsCountQuery> countApprovedParticipants(List<String> moimCodes) {

        return queryFactory
                .select(Projections.constructor(
                        MoimParticipantsCountQuery.class,
                        moimMemberMapping.id.moimCode,
                        moimMemberMapping.count()
                ))
                .from(moimMemberMapping)
                .where(
                        moimMemberMapping.id.moimCode.in(moimCodes),
                        moimMemberMapping.joinYn.eq(YesNo.YES),
                        moimMemberMapping.isApproved.isTrue()
                )
                .groupBy(moimMemberMapping.id.moimCode)
                .fetch();
    }
}