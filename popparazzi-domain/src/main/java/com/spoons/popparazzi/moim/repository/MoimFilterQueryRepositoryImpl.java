package com.spoons.popparazzi.moim.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spoons.popparazzi.category.entity.QCategoryMapping;
import com.spoons.popparazzi.category.entity.QCategoryMaster;
import com.spoons.popparazzi.category.enums.CategoryType;
import com.spoons.popparazzi.common.YesNo;
import com.spoons.popparazzi.like.enums.LikeType;
import com.spoons.popparazzi.moim.dto.command.MoimFilterCommand;
import com.spoons.popparazzi.moim.dto.query.MoimFilterItemQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.spoons.popparazzi.category.entity.QCategoryMapping.categoryMapping;
import static com.spoons.popparazzi.category.entity.QCategoryMaster.categoryMaster;
import static com.spoons.popparazzi.like.entity.QLikeMapping.likeMapping;
import static com.spoons.popparazzi.member.entity.QMember.member;
import static com.spoons.popparazzi.moim.entity.QMoim.moim;
import static com.spoons.popparazzi.popup.entity.QPopup.popup;

@Repository
@RequiredArgsConstructor
public class MoimFilterQueryRepositoryImpl implements MoimFilterQueryRepository {

    private static final int FAVORITE_SIGUNGU_TOP_K = 3;
    private static final int FAVORITE_CATEGORY_TOP_N = 5;
    private static final int FAVORITE_LOOKBACK_DAYS = 30;
    private static final int FAVORITE_REGION_DEFAULT_ORDER = 999;

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<MoimFilterItemQuery> searchMoimsByFilter(MoimFilterCommand command) {
        return switch (command.getViewType()) {
            case NEW -> searchNewest(command);
            case HOT -> searchHot(command);
            case FAVORITE -> searchFavorite(command);
        };
    }

    /**
     * NEW
     * - 삭제되지 않은 모임
     * - 아직 지나지 않은 모임
     * - 필터 적용
     * - 등록일 최신순
     */
    private Slice<MoimFilterItemQuery> searchNewest(MoimFilterCommand command) {
        List<MoimFilterItemQuery> fetched = baseSelect()
                .where(
                        commonAvailableCondition(),
                        regionSidoEq(command),
                        regionSigunguEq(command),
                        dateEq(command),
                        categoryIn(command)
                )
                .orderBy(
                        moim.regDt.desc(),
                        moim.date.asc()
                )
                .offset(getOffset(command))
                .limit(command.getSize() + 1L)
                .fetch();

        return toSlice(fetched, command);
    }

    /**
     * HOT
     * - 오늘 00:00 이후 좋아요 수 기준
     * - 좋아요 수 desc
     * - tie-breaker: 등록일 desc, 일정 asc
     * - 필터 적용
     */
    private Slice<MoimFilterItemQuery> searchHot(MoimFilterCommand command) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        List<MoimFilterItemQuery> fetched = baseSelect()
                .leftJoin(likeMapping).on(
                        likeMapping.targetCode.eq(moim.moimCode),
                        likeMapping.type.eq(LikeType.M),
                        likeMapping.createdAt.goe(todayStart)
                )
                .where(
                        commonAvailableCondition(),
                        regionSidoEq(command),
                        regionSigunguEq(command),
                        dateEq(command),
                        categoryIn(command)
                )
                .groupBy(
                        moim.moimCode,
                        moim.title,
                        popup.addrSido,
                        popup.addrSigungu,
                        moim.date,
                        moim.leaderMemberCode,
                        member.nickname,
                        moim.maxParticipants,
                        moim.regDt
                )
                .orderBy(
                        likeMapping.targetCode.count().desc(),
                        moim.regDt.desc(),
                        moim.date.asc()
                )
                .offset(getOffset(command))
                .limit(command.getSize() + 1L)
                .fetch();

        return toSlice(fetched, command);
    }

    /**
     * FAVORITE
     * - 로그인 사용자 전용
     * - 최근 30일 내 좋아요 데이터 기준 선호 시군구/카테고리 계산
     * - 이미 좋아요한 모임 제외
     * - 우선순위:
     *   1) 선호 시군구 우선순위
     *   2) 선호 카테고리 겹침 수 desc
     *   3) 일정 asc
     *   4) 등록일 desc
     * - 필터 적용
     */
    private Slice<MoimFilterItemQuery> searchFavorite(MoimFilterCommand command) {
        if (command.getMemberCode() == null || command.getMemberCode().isBlank()) {
            return new SliceImpl<>(List.of(), PageRequest.of(command.getPage(), command.getSize()), false);
        }

        List<String> preferredSigungu = findPreferredSigunguTop(command.getMemberCode());
        if (preferredSigungu.isEmpty()) {
            return new SliceImpl<>(List.of(), PageRequest.of(command.getPage(), command.getSize()), false);
        }

        List<String> preferredCategoryCodes = findPreferredCategoryCodes(command.getMemberCode());

        OrderSpecifier<Integer> regionPriorityOrder = buildRegionPriorityOrder(preferredSigungu);
        OrderSpecifier<Long> categoryOverlapOrder = buildCategoryOverlapOrder(preferredCategoryCodes);

        List<MoimFilterItemQuery> fetched = baseSelect()
                .where(
                        commonAvailableCondition(),
                        regionSidoEq(command),
                        regionSigunguEq(command),
                        dateEq(command),
                        categoryIn(command),
                        popup.addrSigungu.in(preferredSigungu),
                        excludeAlreadyLikedMoims(command.getMemberCode())
                )
                .orderBy(
                        regionPriorityOrder,
                        categoryOverlapOrder,
                        moim.date.asc(),
                        moim.regDt.desc()
                )
                .offset(getOffset(command))
                .limit(command.getSize() + 1L)
                .fetch();

        return toSlice(fetched, command);
    }

    /**
     * 공통 select
     * - 카드 기본 뼈대 정보 조회
     * - address는 "시도 + 시군구"
     */
    private com.querydsl.jpa.impl.JPAQuery<MoimFilterItemQuery> baseSelect() {
        return queryFactory
                .select(Projections.constructor(
                        MoimFilterItemQuery.class,
                        moim.moimCode,
                        moim.popupCode,
                        moim.title,
                        popup.addrSido.concat(" ").concat(popup.addrSigungu),
                        moim.date,
                        moim.leaderMemberCode,
                        member.nickname,
                        moim.maxParticipants,
                        moim.regDt
                ))
                .from(moim)
                .join(popup).on(popup.popupCode.eq(moim.popupCode))
                .join(member).on(member.memberCode.eq(moim.leaderMemberCode));
    }

    /**
     * 공통 노출 조건
     * - 삭제되지 않은 모임
     * - 현재 시점 이후 모임
     */
    private BooleanExpression commonAvailableCondition() {
        return moim.deleteYn.eq(YesNo.NO)
                .and(moim.date.goe(LocalDateTime.now()));
    }

    /**
     * 상위 지역 필터
     */
    private BooleanExpression regionSidoEq(MoimFilterCommand command) {
        if (!command.hasRegion()) {
            return null;
        }
        return popup.addrSido.eq(command.getSido());
    }

    /**
     * 하위 지역 필터
     * - "전체"면 조건 제외
     */
    private BooleanExpression regionSigunguEq(MoimFilterCommand command) {
        if (!command.hasSigungu()) {
            return null;
        }

        if ("전체".equals(command.getSigungu())) {
            return null;
        }

        return popup.addrSigungu.eq(command.getSigungu());
    }

    /**
     * 일정 필터
     * - 선택한 날짜에 열리는 모임만 조회
     * - 시간은 무시하고 하루 범위 비교
     */
    private BooleanExpression dateEq(MoimFilterCommand command) {
        if (!command.hasDate()) {
            return null;
        }

        LocalDateTime startOfDay = command.getDate().atStartOfDay();
        LocalDateTime nextDay = command.getDate().plusDays(1).atStartOfDay();

        return moim.date.goe(startOfDay)
                .and(moim.date.lt(nextDay));
    }

    /**
     * 카테고리 필터
     * - 다중 선택
     * - OR 조건
     * - exists 서브쿼리로 중복 방지
     */
    private BooleanExpression categoryIn(MoimFilterCommand command) {
        if (!command.hasCategory()) {
            return null;
        }

        return JPAExpressions
                .selectOne()
                .from(categoryMapping)
                .join(categoryMaster).on(categoryMaster.code.eq(categoryMapping.categoryCode))
                .where(
                        categoryMapping.parentCode.eq(moim.moimCode),
                        categoryMapping.categoryCode.in(command.getCategoryCodes()),
                        categoryMaster.type.eq(CategoryType.M)
                )
                .exists();
    }

    /**
     * FAVORITE - 이미 좋아요한 모임 제외
     */
    private BooleanExpression excludeAlreadyLikedMoims(String memberCode) {
        return moim.moimCode.notIn(
                JPAExpressions
                        .select(likeMapping.targetCode)
                        .from(likeMapping)
                        .where(
                                likeMapping.memberCode.eq(memberCode),
                                likeMapping.type.eq(LikeType.M)
                        )
        );
    }

    /**
     * FAVORITE - 선호 시군구 TopK
     */
    private List<String> findPreferredSigunguTop(String memberCode) {
        LocalDateTime from = LocalDateTime.now().minusDays(FAVORITE_LOOKBACK_DAYS);

        return queryFactory
                .select(popup.addrSigungu)
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
                .orderBy(likeMapping.targetCode.count().desc())
                .limit(FAVORITE_SIGUNGU_TOP_K)
                .fetch();
    }

    /**
     * FAVORITE - 선호 카테고리 TopN
     */
    private List<String> findPreferredCategoryCodes(String memberCode) {
        LocalDateTime from = LocalDateTime.now().minusDays(FAVORITE_LOOKBACK_DAYS);

        return queryFactory
                .select(categoryMaster.code)
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
                .limit(FAVORITE_CATEGORY_TOP_N)
                .fetch();
    }

    /**
     * FAVORITE - 시군구 우선순위 정렬
     * ex) [성동구, 강남구, 마포구]
     * => 성동구 0, 강남구 1, 마포구 2, 나머지 999
     */
    private OrderSpecifier<Integer> buildRegionPriorityOrder(List<String> preferredSigungu) {
        CaseBuilder.Cases<Integer, com.querydsl.core.types.dsl.NumberExpression<Integer>> regionCase =
                new CaseBuilder()
                        .when(popup.addrSigungu.eq(preferredSigungu.get(0))).then(0);

        for (int i = 1; i < preferredSigungu.size(); i++) {
            regionCase = regionCase.when(popup.addrSigungu.eq(preferredSigungu.get(i))).then(i);
        }

        return regionCase.otherwise(FAVORITE_REGION_DEFAULT_ORDER).asc();
    }

    /**
     * FAVORITE - 선호 카테고리 겹침 수 정렬
     */
    private OrderSpecifier<Long> buildCategoryOverlapOrder(List<String> preferredCategoryCodes) {
        if (preferredCategoryCodes == null || preferredCategoryCodes.isEmpty()) {
            return new OrderSpecifier<>(Order.DESC, Expressions.constant(0L));
        }

        QCategoryMapping overlapCategoryMapping = new QCategoryMapping("overlapCategoryMapping");
        QCategoryMaster overlapCategoryMaster = new QCategoryMaster("overlapCategoryMaster");

        SubQueryExpression<Long> overlapCountSubQuery = JPAExpressions
                .select(overlapCategoryMapping.categoryCode.count())
                .from(overlapCategoryMapping)
                .join(overlapCategoryMaster).on(overlapCategoryMaster.code.eq(overlapCategoryMapping.categoryCode))
                .where(
                        overlapCategoryMapping.parentCode.eq(moim.moimCode),
                        overlapCategoryMapping.categoryCode.in(preferredCategoryCodes),
                        overlapCategoryMaster.type.eq(CategoryType.M)
                );

        return new OrderSpecifier<>(Order.DESC, overlapCountSubQuery);
    }

    private long getOffset(MoimFilterCommand command) {
        return (long) command.getPage() * command.getSize();
    }

    private Slice<MoimFilterItemQuery> toSlice(List<MoimFilterItemQuery> fetched, MoimFilterCommand command) {
        boolean hasNext = fetched.size() > command.getSize();

        List<MoimFilterItemQuery> contents = hasNext
                ? fetched.subList(0, command.getSize())
                : fetched;

        return new SliceImpl<>(
                contents,
                PageRequest.of(command.getPage(), command.getSize()),
                hasNext
        );
    }
}