package com.spoons.popparazzi.moim.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spoons.popparazzi.category.entity.QCategoryMapping;
import com.spoons.popparazzi.category.entity.QCategoryMaster;
import com.spoons.popparazzi.category.enums.CategoryType;
import com.spoons.popparazzi.common.YesNo;
import com.spoons.popparazzi.like.enums.LikeType;
import com.spoons.popparazzi.moim.dto.command.MoimFilterCommand;
import com.spoons.popparazzi.moim.dto.query.*;
import com.spoons.popparazzi.moim.dto.query.main.*;
import com.spoons.popparazzi.moim.dto.result.HotMoimCardResult;
import com.spoons.popparazzi.moim.dto.result.MoimApplyInfoResult;
import com.spoons.popparazzi.moim.enums.MoimMemberStatus;
import com.spoons.popparazzi.util.PaginationInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.spoons.popparazzi.category.entity.QCategoryMapping.categoryMapping;
import static com.spoons.popparazzi.category.entity.QCategoryMaster.categoryMaster;
import static com.spoons.popparazzi.like.entity.QLikeMapping.likeMapping;
import static com.spoons.popparazzi.member.entity.QMember.member;
import static com.spoons.popparazzi.moim.entity.QMoim.moim;
import static com.spoons.popparazzi.moim.entity.QMoimMemberMapping.moimMemberMapping;
import static com.spoons.popparazzi.popup.entity.QPopup.popup;

@Repository
@RequiredArgsConstructor
public class MoimQueryRepository {

    private static final int FAVORITE_SIGUNGU_TOP_K = 3;
    private static final int FAVORITE_CATEGORY_TOP_N = 5;
    private static final int FAVORITE_LOOKBACK_DAYS = 30;
    private static final int FAVORITE_REGION_DEFAULT_ORDER = 999;

    private final JPAQueryFactory queryFactory;

    // ─────────────────────────────────────────────────────────────────────────
    // 상세 / 신청 / 참여자
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @methodName  : findMoimDetail
     * @author      : seulgi Yang
     * @param       : moimCode
     * @returnType  : MoimDetailQuery
     * @desc        : 모임 상세 조회. 삭제된 모임 제외. 방장 프로필 URL leftJoin
     */
    public MoimDetailQuery findMoimDetail(String moimCode) {
        return queryFactory
                .select(Projections.constructor(
                        MoimDetailQuery.class,
                        moim.moimCode,
                        moim.title,
                        moim.body,
                        moim.date,
                        moim.maxParticipants,
                        moim.leaderMemberCode,
                        member.profileUrl
                ))
                .from(moim)
                .leftJoin(member).on(member.memberCode.eq(moim.leaderMemberCode))
                .where(
                        moim.moimCode.eq(moimCode),
                        moim.deleteYn.eq(YesNo.NO)
                )
                .fetchOne();
    }

    /**
     * @methodName  : findApplyInfoByMoimCode
     * @author      : seulgi Yang
     * @param       : moimCode
     * @returnType  : Optional<MoimApplyInfoResult>
     * @desc        : 모임 신청 화면 조회. 방장 프로필 + 사전 질문 반환. 삭제된 모임 제외
     */
    public Optional<MoimApplyInfoResult> findApplyInfoByMoimCode(String moimCode) {
        return Optional.ofNullable(
                queryFactory
                        .select(Projections.constructor(
                                MoimApplyInfoResult.class,
                                member.profileUrl,
                                member.nickname,
                                moim.preQuestion
                        ))
                        .from(moim)
                        .join(member).on(moim.leaderMemberCode.eq(member.memberCode))
                        .where(
                                moim.moimCode.eq(moimCode),
                                moim.deleteYn.eq(YesNo.NO)
                        )
                        .fetchOne()
        );
    }

    /**
     * @methodName  : findParticipantsByMoimCode
     * @author      : seulgi Yang
     * @param       : moimCode
     * @returnType  : List<MoimParticipantQuery>
     * @desc        : 모임 참여자 목록 조회. APPROVED 상태만 포함. 방장 우선 정렬 후 닉네임 오름차순
     */
    public List<MoimParticipantQuery> findParticipantsByMoimCode(String moimCode) {
        NumberExpression<Integer> leaderOrder = new CaseBuilder()
                .when(member.memberCode.eq(moim.leaderMemberCode)).then(1)
                .otherwise(0);

        BooleanExpression isLeader = member.memberCode.eq(moim.leaderMemberCode);

        return queryFactory
                .select(Projections.constructor(
                        MoimParticipantQuery.class,
                        member.memberCode,
                        member.nickname,
                        member.bio,
                        member.profileUrl,
                        isLeader
                ))
                .from(moimMemberMapping)
                .join(member).on(moimMemberMapping.id.memberCode.eq(member.memberCode))
                .join(moim).on(moimMemberMapping.id.moimCode.eq(moim.moimCode))
                .where(
                        moimMemberMapping.id.moimCode.eq(moimCode),
                        moimMemberMapping.status.eq(MoimMemberStatus.APPROVED),
                        moim.deleteYn.eq(YesNo.NO)
                )
                .orderBy(leaderOrder.desc(), member.nickname.asc())
                .fetch();
    }

    /**
     * @methodName  : countApprovedParticipants
     * @author      : seulgi Yang
     * @param       : moimCodes
     * @returnType  : List<MoimParticipantsCountQuery>
     * @desc        : 여러 모임의 승인 완료 참여자 수 bulk 집계. 카드 목록 렌더링용
     */
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
                        moimMemberMapping.status.eq(MoimMemberStatus.APPROVED)
                )
                .groupBy(moimMemberMapping.id.moimCode)
                .fetch();
    }

    /**
     * @methodName  : findParticipantProfiles
     * @author      : seulgi Yang
     * @param       : moimCodes
     * @returnType  : List<MoimParticipantProfileQuery>
     * @desc        : 여러 모임의 참여자 프로필 URL bulk 조회. 방장 우선 정렬. 카드 썸네일용 (최대 2명 노출은 Service에서 처리)
     */
    public List<MoimParticipantProfileQuery> findParticipantProfiles(List<String> moimCodes) {
        if (moimCodes == null || moimCodes.isEmpty()) return List.of();

        NumberExpression<Integer> leaderOrder = new CaseBuilder()
                .when(member.memberCode.eq(moim.leaderMemberCode)).then(1)
                .otherwise(0);

        return queryFactory
                .select(Projections.constructor(
                        MoimParticipantProfileQuery.class,
                        moimMemberMapping.id.moimCode,
                        member.profileUrl
                ))
                .from(moimMemberMapping)
                .join(member).on(moimMemberMapping.id.memberCode.eq(member.memberCode))
                .join(moim).on(moimMemberMapping.id.moimCode.eq(moim.moimCode))
                .where(
                        moimMemberMapping.id.moimCode.in(moimCodes),
                        moimMemberMapping.status.eq(MoimMemberStatus.APPROVED),
                        moim.deleteYn.eq(YesNo.NO)
                )
                .orderBy(
                        moimMemberMapping.id.moimCode.asc(),
                        leaderOrder.desc(),
                        member.nickname.asc()
                )
                .fetch();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 메인 화면
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @methodName  : findNewestForMain
     * @author      : seulgi Yang
     * @param       : pageable
     * @returnType  : List<NewestMoimItemQuery>
     * @desc        : 메인 화면 신규 모임 조회. 삭제된 모임 제외. 등록일 최신순
     */
    public List<NewestMoimItemQuery> findNewestForMain(Pageable pageable) {
        return queryFactory
                .select(Projections.constructor(
                        NewestMoimItemQuery.class,
                        moim.moimCode,
                        moim.popupCode,
                        moim.title,
                        moim.date,
                        moim.maxParticipants
                ))
                .from(moim)
                .where(moim.deleteYn.eq(YesNo.NO))
                .orderBy(moim.regDt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    /**
     * @methodName  : findHotCardsBase
     * @author      : seulgi Yang
     * @param       : moimCodes
     * @returnType  : List<HotMoimCardResult>
     * @desc        : 핫 모임 카드 기본 정보 bulk 조회. 승인 완료 참여자 수 포함. 썸네일/좋아요수는 Service에서 조립
     */
    public List<HotMoimCardResult> findHotCardsBase(List<String> moimCodes) {
        if (moimCodes == null || moimCodes.isEmpty()) return List.of();

        NumberExpression<Integer> joinedCount =
                moimMemberMapping.id.memberCode.count().intValue();

        return queryFactory
                .select(Projections.constructor(
                        HotMoimCardResult.class,
                        moim.moimCode,
                        moim.popupCode,
                        moim.title,
                        moim.date,
                        joinedCount,
                        moim.maxParticipants,
                        Expressions.nullExpression(String.class),
                        Expressions.constant(0L)
                ))
                .from(moim)
                .leftJoin(moimMemberMapping).on(
                        moimMemberMapping.id.moimCode.eq(moim.moimCode),
                        moimMemberMapping.status.eq(MoimMemberStatus.APPROVED)
                )
                .where(
                        moim.moimCode.in(moimCodes),
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

    /**
     * @methodName  : findPreferredSigunguTop
     * @author      : seulgi Yang
     * @param       : memberCode, days, topK
     * @returnType  : List<PreferredSigunguQuery>
     * @desc        : 최근 N일간 좋아요한 모임(LikeType=M) 기준 선호 시군구 TopK 반환. 삭제된 모임 제외
     */
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
     * @methodName  : findRecommendMoimCandidates
     * @author      : seulgi Yang
     * @param       : sigunguPriority, memberCode, limit
     * @returnType  : List<RecommendedMoimBaseQuery>
     * @desc        : 선호 시군구 기반 추천 후보 모임 조회.
     *               현재 시각 + 24시간 이후 일정만 포함. 내가 좋아요한 모임 제외. 삭제된 모임 제외
     */
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
                                JPAExpressions
                                        .select(likeMapping.targetCode)
                                        .from(likeMapping)
                                        .where(
                                                likeMapping.memberCode.eq(memberCode),
                                                likeMapping.type.eq(LikeType.M)
                                        )
                        )
                )
                .orderBy(moim.date.asc(), moim.regDt.desc())
                .limit(limit)
                .fetch();
    }

    /**
     * @methodName  : findPreferredCategories
     * @author      : seulgi Yang
     * @param       : memberCode, days, topN
     * @returnType  : List<PreferredCategoryQuery>
     * @desc        : 최근 N일간 좋아요한 모임 기준 선호 카테고리 TopN 반환. CategoryType=M만 집계
     */
    public List<PreferredCategoryQuery> findPreferredCategories(String memberCode, int days, int topN) {
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
     * @methodName  : findMoimCategories
     * @author      : seulgi Yang
     * @param       : moimCodes
     * @returnType  : List<MoimCategoryLinkQuery>
     * @desc        : 여러 모임의 카테고리 (모임코드, 카테고리코드) bulk 조회. CategoryType=M만 포함
     */
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

    // ─────────────────────────────────────────────────────────────────────────
    // 검색
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @methodName  : searchMoims
     * @author      : seulgi Yang
     * @param       : normalizedKeyword, paginationInfo
     * @returnType  : List<MoimSearchItemQuery>
     * @desc        : 모임 키워드 검색. 제목/내용 공백 제거 후 like 비교.
     *               삭제된 모임 제외. 마감되지 않은 모임(정원 미달)만 포함. 등록일 최신순
     */
    public List<MoimSearchItemQuery> searchMoims(String normalizedKeyword, PaginationInfo paginationInfo) {
        return queryFactory
                .select(Projections.constructor(
                        MoimSearchItemQuery.class,
                        moim.moimCode,
                        moim.popupCode,
                        moim.title,
                        popupAddress(),
                        moim.leaderMemberCode,
                        member.nickname,
                        moim.maxParticipants
                ))
                .from(moim)
                .join(member).on(member.memberCode.eq(moim.leaderMemberCode))
                .join(popup).on(popup.popupCode.eq(moim.popupCode))
                .where(
                        moim.deleteYn.eq(YesNo.NO),
                        keywordContains(normalizedKeyword),
                        isOpenMoim()
                )
                .orderBy(moim.regDt.desc())
                .offset(paginationInfo.getFirstRecordIndex())
                .limit(paginationInfo.getRecordCountPerPage())
                .fetch();
    }

    /**
     * @methodName  : countSearchMoims
     * @author      : seulgi Yang
     * @param       : normalizedKeyword
     * @returnType  : long
     * @desc        : 모임 키워드 검색 결과 총 건수. 페이징 처리용
     */
    public long countSearchMoims(String normalizedKeyword) {
        Long count = queryFactory
                .select(moim.count())
                .from(moim)
                .where(
                        moim.deleteYn.eq(YesNo.NO),
                        keywordContains(normalizedKeyword),
                        isOpenMoim()
                )
                .fetchOne();

        return count == null ? 0L : count;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 필터
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @methodName  : searchMoimsByFilter
     * @author      : seulgi Yang
     * @param       : MoimFilterCommand
     * @returnType  : Slice<MoimFilterItemQuery>
     * @desc        : viewType에 따라 NEW / HOT / FAVORITE 필터 조회 분기.
     *               FAVORITE는 비로그인 또는 선호 데이터 없으면 빈 슬라이스 반환
     */
    public Slice<MoimFilterItemQuery> searchMoimsByFilter(MoimFilterCommand command) {
        return switch (command.getViewType()) {
            case NEW -> searchNewest(command);
            case HOT -> searchHot(command);
            case FAVORITE -> searchFavorite(command);
        };
    }

    /**
     * NEW 필터 - 삭제되지 않은 모임, 현재 시각 이후 일정, 등록일 최신순
     */
    private Slice<MoimFilterItemQuery> searchNewest(MoimFilterCommand command) {
        List<MoimFilterItemQuery> fetched = filterBaseSelect()
                .where(
                        commonAvailableCondition(),
                        regionSidoEq(command),
                        regionSigunguEq(command),
                        dateEq(command),
                        categoryIn(command)
                )
                .orderBy(moim.regDt.desc(), moim.date.asc())
                .offset(getOffset(command))
                .limit(command.getSize() + 1L)
                .fetch();

        return toSlice(fetched, command);
    }

    /**
     * HOT 필터 - 오늘 00:00 이후 좋아요 수 desc, tie-breaker: 등록일 desc / 일정 asc
     */
    private Slice<MoimFilterItemQuery> searchHot(MoimFilterCommand command) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        List<MoimFilterItemQuery> fetched = filterBaseSelect()
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
                        moim.moimCode, moim.title, popup.addrSido, popup.addrSigungu,
                        moim.date, moim.leaderMemberCode, member.nickname,
                        moim.maxParticipants, moim.regDt
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
     * FAVORITE 필터 - 로그인 사용자 전용.
     * 최근 30일 좋아요 기준 선호 시군구/카테고리 계산.
     * 이미 좋아요한 모임 제외.
     * 정렬: 선호 시군구 우선순위 → 카테고리 겹침 수 desc → 일정 asc → 등록일 desc
     */
    private Slice<MoimFilterItemQuery> searchFavorite(MoimFilterCommand command) {
        if (command.getMemberCode() == null || command.getMemberCode().isBlank()) {
            return emptySlice(command);
        }

        List<String> preferredSigungu = findPreferredSigunguCodes(command.getMemberCode());
        if (preferredSigungu.isEmpty()) return emptySlice(command);

        List<String> preferredCategoryCodes = findPreferredCategoryCodesRaw(command.getMemberCode());

        List<MoimFilterItemQuery> fetched = filterBaseSelect()
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
                        buildRegionPriorityOrder(preferredSigungu),
                        buildCategoryOverlapOrder(preferredCategoryCodes),
                        moim.date.asc(),
                        moim.regDt.desc()
                )
                .offset(getOffset(command))
                .limit(command.getSize() + 1L)
                .fetch();

        return toSlice(fetched, command);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 필터 내부 헬퍼
    // ─────────────────────────────────────────────────────────────────────────

    private JPAQuery<MoimFilterItemQuery> filterBaseSelect() {
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

    private BooleanExpression commonAvailableCondition() {
        return moim.deleteYn.eq(YesNo.NO).and(moim.date.goe(LocalDateTime.now()));
    }

    private BooleanExpression regionSidoEq(MoimFilterCommand command) {
        if (!command.hasRegion()) return null;
        return popup.addrSido.eq(command.getSido());
    }

    private BooleanExpression regionSigunguEq(MoimFilterCommand command) {
        if (!command.hasSigungu()) return null;
        if ("전체".equals(command.getSigungu())) return null;
        return popup.addrSigungu.eq(command.getSigungu());
    }

    private BooleanExpression dateEq(MoimFilterCommand command) {
        if (!command.hasDate()) return null;
        LocalDateTime startOfDay = command.getDate().atStartOfDay();
        LocalDateTime nextDay = command.getDate().plusDays(1).atStartOfDay();
        return moim.date.goe(startOfDay).and(moim.date.lt(nextDay));
    }

    private BooleanExpression categoryIn(MoimFilterCommand command) {
        if (!command.hasCategory()) return null;

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

    private List<String> findPreferredSigunguCodes(String memberCode) {
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

    private List<String> findPreferredCategoryCodesRaw(String memberCode) {
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

    private OrderSpecifier<Integer> buildRegionPriorityOrder(List<String> preferredSigungu) {
        CaseBuilder.Cases<Integer, NumberExpression<Integer>> regionCase =
                new CaseBuilder()
                        .when(popup.addrSigungu.eq(preferredSigungu.get(0))).then(0);

        for (int i = 1; i < preferredSigungu.size(); i++) {
            regionCase = regionCase.when(popup.addrSigungu.eq(preferredSigungu.get(i))).then(i);
        }

        return regionCase.otherwise(FAVORITE_REGION_DEFAULT_ORDER).asc();
    }

    private OrderSpecifier<Long> buildCategoryOverlapOrder(List<String> preferredCategoryCodes) {
        if (preferredCategoryCodes == null || preferredCategoryCodes.isEmpty()) {
            return new OrderSpecifier<>(Order.DESC, Expressions.constant(0L));
        }

        QCategoryMapping overlapMapping = new QCategoryMapping("overlapCategoryMapping");
        QCategoryMaster overlapMaster = new QCategoryMaster("overlapCategoryMaster");

        SubQueryExpression<Long> overlapCount = JPAExpressions
                .select(overlapMapping.categoryCode.count())
                .from(overlapMapping)
                .join(overlapMaster).on(overlapMaster.code.eq(overlapMapping.categoryCode))
                .where(
                        overlapMapping.parentCode.eq(moim.moimCode),
                        overlapMapping.categoryCode.in(preferredCategoryCodes),
                        overlapMaster.type.eq(CategoryType.M)
                );

        return new OrderSpecifier<>(Order.DESC, overlapCount);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 검색 내부 헬퍼
    // ─────────────────────────────────────────────────────────────────────────

    private BooleanExpression isOpenMoim() {
        return moim.date.after(LocalDateTime.now()).and(hasAvailableSlots());
    }

    private BooleanExpression hasAvailableSlots() {
        return JPAExpressions
                .select(moimMemberMapping.count())
                .from(moimMemberMapping)
                .where(
                        moimMemberMapping.id.moimCode.eq(moim.moimCode),
                        moimMemberMapping.status.eq(MoimMemberStatus.APPROVED)
                )
                .lt(moim.maxParticipants.longValue());
    }

    private BooleanExpression keywordContains(String normalizedKeyword) {
        if (normalizedKeyword == null || normalizedKeyword.isBlank()) return null;
        String likeKeyword = "%" + normalizedKeyword + "%";
        return normalizedTitle().like(likeKeyword).or(normalizedBody().like(likeKeyword));
    }

    private StringExpression normalizedTitle() {
        return Expressions.stringTemplate("replace({0}, ' ', '')", moim.title);
    }

    private StringExpression normalizedBody() {
        return Expressions.stringTemplate("replace({0}, ' ', '')", moim.body);
    }

    private StringExpression popupAddress() {
        return Expressions.stringTemplate("concat({0}, ' ', {1})", popup.addrSido, popup.addrSigungu);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 공통 유틸
    // ─────────────────────────────────────────────────────────────────────────

    private long getOffset(MoimFilterCommand command) {
        return (long) command.getPage() * command.getSize();
    }

    private Slice<MoimFilterItemQuery> toSlice(List<MoimFilterItemQuery> fetched, MoimFilterCommand command) {
        boolean hasNext = fetched.size() > command.getSize();
        List<MoimFilterItemQuery> contents = hasNext ? fetched.subList(0, command.getSize()) : fetched;
        return new SliceImpl<>(contents, PageRequest.of(command.getPage(), command.getSize()), hasNext);
    }

    private Slice<MoimFilterItemQuery> emptySlice(MoimFilterCommand command) {
        return new SliceImpl<>(List.of(), PageRequest.of(command.getPage(), command.getSize()), false);
    }
}
