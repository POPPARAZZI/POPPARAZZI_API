package com.spoons.popparazzi.moim.service;

import com.spoons.popparazzi.common.YesNo;
import com.spoons.popparazzi.error.exception.BusinessException;
import com.spoons.popparazzi.file.dto.query.FileDetailQuery;
import com.spoons.popparazzi.file.dto.query.MoimThumbTarget;
import com.spoons.popparazzi.file.enums.FileType;
import com.spoons.popparazzi.file.repository.FileQueryRepository;
import com.spoons.popparazzi.file.service.FileThumbnailService;
import com.spoons.popparazzi.like.dto.query.LikeRankQuery;
import com.spoons.popparazzi.like.enums.LikeType;
import com.spoons.popparazzi.like.repository.LikeQueryRepository;
import com.spoons.popparazzi.like.repository.LikeRepository;
import com.spoons.popparazzi.moim.dto.command.MoimFilterCommand;
import com.spoons.popparazzi.moim.dto.query.MoimDetailQuery;
import com.spoons.popparazzi.moim.dto.query.MoimFilterItemQuery;
import com.spoons.popparazzi.moim.dto.query.main.*;
import com.spoons.popparazzi.moim.dto.result.*;
import com.spoons.popparazzi.moim.enums.MoimViewType;
import com.spoons.popparazzi.moim.error.MoimErrorCode;
import com.spoons.popparazzi.moim.repository.MoimFilterQueryRepository;
import com.spoons.popparazzi.moim.repository.MoimMainRepository;
import com.spoons.popparazzi.moim.repository.MoimMemberMappingRepository;
import com.spoons.popparazzi.moim.repository.MoimQueryRepository;
import com.spoons.popparazzi.moim.service.support.MoimAccessSupportService;
import com.spoons.popparazzi.moim.service.support.MoimCardSupportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class MoimServiceImpl implements MoimService {

    private static final int NEWEST_DEFAULT_LIMIT = 3;
    private static final int HOT_DEFAULT_LIMIT = 10;
    private static final int LIMIT_MAX = 50;

    private final MoimQueryRepository moimQueryRepository;
    private final MoimMainRepository moimMainRepository;
    private final MoimMemberMappingRepository moimMemberMappingRepository;
    private final MoimFilterQueryRepository moimFilterQueryRepository;
    private final FileQueryRepository fileQueryRepository;
    private final LikeRepository likeRepository;
    private final LikeQueryRepository likeQueryRepository;

    private final MoimCardSupportService moimCardSupportService;
    private final MoimAccessSupportService moimAccessSupportService;
    private final FileThumbnailService fileThumbnailService;




    // =========================
    // 신규 모임
    // =========================
    @Override
    public List<NewestMoimCardResult> getNewestMoimsForMain(int limit, String memberCode) {

        int fixedLimit = normalizeLimit(limit, NEWEST_DEFAULT_LIMIT);

        List<NewestMoimItemQuery> items =
                moimMainRepository.findNewestForMain(PageRequest.of(0, fixedLimit));

        if (items.isEmpty()) return List.of();

        return buildNewestCards(items, memberCode);
    }

    // =========================
    // 핫 모임 (오늘 00:00 기준) + 폴백(최신)
    // =========================
    @Override
    public List<HotMoimCardResult> getHotMoimCardsForMain(int limit) {

        int fixedLimit = normalizeLimit(limit, HOT_DEFAULT_LIMIT);

        LocalDateTime since = LocalDate.now().atStartOfDay();

        List<LikeRankQuery> ranks = likeQueryRepository.findTopRankKeys(
                LikeType.M,
                since,
                PageRequest.of(0, fixedLimit)
        );

        // 폴백 1: 랭킹 자체가 없으면 최신 모임으로 대체
        if (ranks.isEmpty()) {
            return buildLatestHotFallback(fixedLimit);
        }

        List<HotMoimCardResult> hotCards = buildHotCardsFromRanks(ranks);

        // 폴백 2: 랭킹은 있는데 베이스가 비면(삭제/꼬임) 최신으로 대체
        if (hotCards.isEmpty()) {
            return buildLatestHotFallback(fixedLimit);
        }

        return hotCards;
    }

    // =========================
    // 추천 모임
    // =========================
    @Override
    public List<MoimRecommendCardResult> recommendForMember(String memberCode) {

        List<String> sigunguPriority = moimMainRepository
                .findPreferredSigunguTop(memberCode, 30, 3)
                .stream()
                .map(PreferredSigunguQuery::sigungu)
                .toList();

        List<RecommendedMoimBaseQuery> candidates = sigunguPriority.isEmpty()
                ? List.of()
                : moimMainRepository.findRecommendMoimCandidates(sigunguPriority, memberCode, 20);

        // 폴백: 추천 후보가 없으면 최신 모임으로 대체
        if (candidates.isEmpty()) {
            return buildLatestRecommendFallback(memberCode, 10);
        }

        return buildRecommendCards(memberCode, sigunguPriority, candidates);
    }

    // =========================
    // 신규 카드 조립
    // =========================
    private List<NewestMoimCardResult> buildNewestCards(List<NewestMoimItemQuery> items, String memberCode) {

        List<String> moimCodes = items.stream()
                .map(NewestMoimItemQuery::moimCode)
                .toList();

        Set<String> likedSet = moimCardSupportService.getLikedMoimCodes(memberCode, moimCodes);
        Map<String, List<String>> categoryMap = moimCardSupportService.getMoimCategories(moimCodes);
        Map<String, List<String>> profileMap = moimCardSupportService.getParticipantProfileUrls(moimCodes);

        Map<String, String> thumbMap = fileThumbnailService.getMoimThumbsWithPopupFallback(
                toThumbTargets(items, NewestMoimItemQuery::moimCode, NewestMoimItemQuery::popupCode)
        );

        return items.stream()
                .map(it -> new NewestMoimCardResult(
                        it.moimCode(),
                        it.popupCode(),
                        it.title(),
                        it.moimDate(),
                        it.maxParticipantCount(),
                        thumbMap.get(it.moimCode()),
                        likedSet.contains(it.moimCode()),
                        categoryMap.getOrDefault(it.moimCode(), List.of()),
                        profileMap.getOrDefault(it.moimCode(), List.of())
                ))
                .toList();
    }

    // =========================
    // 핫 카드 조립 (ranks 순서 유지)
    // =========================
    private List<HotMoimCardResult> buildHotCardsFromRanks(List<LikeRankQuery> ranks) {

        List<String> mmCodes = ranks.stream()
                .map(LikeRankQuery::targetCode)
                .toList();

        List<HotMoimCardResult> bases = moimMainRepository.findHotCardsBase(mmCodes);
        if (bases.isEmpty()) return List.of();

        Map<String, HotMoimCardResult> baseMap = bases.stream()
                .collect(Collectors.toMap(HotMoimCardResult::moimCode, it -> it));

        Map<String, String> thumbMap = fileThumbnailService.getMoimThumbsWithPopupFallback(
                toThumbTargets(bases, HotMoimCardResult::moimCode, HotMoimCardResult::popupCode)
        );

        return ranks.stream()
                .map(rank -> {
                    HotMoimCardResult base = baseMap.get(rank.targetCode());
                    if (base == null) return null;

                    return new HotMoimCardResult(
                            base.moimCode(),
                            base.popupCode(),
                            base.title(),
                            base.date(),
                            base.currentParticipants(),
                            base.maxParticipants(),
                            thumbMap.get(base.moimCode()),
                            rank.likeCountToday()
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }

    // =========================
    // 핫 폴백: 최신 모임을 Hot 카드 형태로 변환
    // =========================
    private List<HotMoimCardResult> buildLatestHotFallback(int limit) {

        List<NewestMoimItemQuery> latest = getLatestMoims(limit);
        if (latest.isEmpty()) return List.of();

        List<String> moimCodes = latest.stream()
                .map(NewestMoimItemQuery::moimCode)
                .toList();

        Map<String, Integer> participantMap =
                moimCardSupportService.getParticipantCounts(moimCodes);

        Map<String, String> thumbMap = fileThumbnailService.getMoimThumbsWithPopupFallback(
                toThumbTargets(latest, NewestMoimItemQuery::moimCode, NewestMoimItemQuery::popupCode)
        );

        // 폴백이므로 likeCountToday는 0으로
        return latest.stream()
                .map(it -> new HotMoimCardResult(
                        it.moimCode(),
                        it.popupCode(),
                        it.title(),
                        it.moimDate(),
                        participantMap.getOrDefault(it.moimCode(), 0),
                        it.maxParticipantCount(),
                        thumbMap.get(it.moimCode()),
                        0L
                ))
                .toList();
    }

    // =========================
    // 추천 카드 조립
    // =========================
    private List<MoimRecommendCardResult> buildRecommendCards(
            String memberCode,
            List<String> sigunguPriority,
            List<RecommendedMoimBaseQuery> candidates
    ) {

        List<String> moimCodes = candidates.stream()
                .map(RecommendedMoimBaseQuery::moimCode)
                .toList();

        Map<String, Integer> participantMap =
                moimCardSupportService.getParticipantCounts(moimCodes);

        Set<String> likedSet =
                moimCardSupportService.getLikedMoimCodes(memberCode, moimCodes);

        Set<String> preferredCategorySet = moimMainRepository
                .findPreferredCategories(memberCode, 30, 5)
                .stream()
                .map(PreferredCategoryQuery::categoryCode)
                .collect(Collectors.toSet());

        Map<String, Set<String>> moimCategoryMap = moimMainRepository
                .findMoimCategories(moimCodes)
                .stream()
                .collect(Collectors.groupingBy(
                        MoimCategoryLinkQuery::moimCode,
                        Collectors.mapping(MoimCategoryLinkQuery::categoryCode, Collectors.toSet())
                ));

        List<RecommendedMoimBaseQuery> sorted = candidates.stream()
                .sorted((a, b) -> compareRecommend(a, b, sigunguPriority, moimCategoryMap, preferredCategorySet))
                .limit(10)
                .toList();

        Map<String, String> thumbMap = fileThumbnailService.getMoimThumbsWithPopupFallback(
                toThumbTargets(sorted, RecommendedMoimBaseQuery::moimCode, RecommendedMoimBaseQuery::popupCode)
        );

        return sorted.stream()
                .map(m -> new MoimRecommendCardResult(
                        m.moimCode(),
                        m.title(),
                        m.date(),
                        participantMap.getOrDefault(m.moimCode(), 0),
                        m.maxParticipants(),
                        thumbMap.get(m.moimCode()),
                        likedSet.contains(m.moimCode())
                ))
                .toList();
    }

    // =========================
    // 추천 폴백: 최신 모임을 Recommend 카드 형태로 변환
    // =========================
    private List<MoimRecommendCardResult> buildLatestRecommendFallback(String memberCode, int limit) {

        List<NewestMoimItemQuery> latest = getLatestMoims(limit);
        if (latest.isEmpty()) return List.of();

        List<String> moimCodes = latest.stream()
                .map(NewestMoimItemQuery::moimCode)
                .toList();

        Set<String> likedSet =
                moimCardSupportService.getLikedMoimCodes(memberCode, moimCodes);

        Map<String, String> thumbMap = fileThumbnailService.getMoimThumbsWithPopupFallback(
                toThumbTargets(latest, NewestMoimItemQuery::moimCode, NewestMoimItemQuery::popupCode)
        );

        return latest.stream()
                .map(it -> new MoimRecommendCardResult(
                        it.moimCode(),
                        it.title(),
                        it.moimDate(),
                        0,
                        it.maxParticipantCount(),
                        thumbMap.get(it.moimCode()),
                        likedSet.contains(it.moimCode())
                ))
                .toList();
    }

    // =========================
    // 공통: 최신 모임 N개 조회
    // =========================
    private List<NewestMoimItemQuery> getLatestMoims(int limit) {
        return moimMainRepository.findNewestForMain(PageRequest.of(0, limit));
    }

    // =========================
    // 공통: 썸네일 타겟 생성
    // =========================
    private <T> List<MoimThumbTarget> toThumbTargets(
            List<T> items,
            java.util.function.Function<T, String> moimCodeGetter,
            java.util.function.Function<T, String> popupCodeGetter
    ) {
        if (items == null || items.isEmpty()) return List.of();

        return items.stream()
                .map(it -> new MoimThumbTarget(moimCodeGetter.apply(it), popupCodeGetter.apply(it)))
                .toList();
    }

    // =========================
    // 추천 정렬
    // =========================
    private int compareRecommend(
            RecommendedMoimBaseQuery a,
            RecommendedMoimBaseQuery b,
            List<String> sigunguPriority,
            Map<String, Set<String>> moimCategoryMap,
            Set<String> preferredCategorySet
    ) {
        int regionScoreA = sigunguPriority.indexOf(a.sigungu());
        int regionScoreB = sigunguPriority.indexOf(b.sigungu());
        if (regionScoreA != regionScoreB) {
            return Integer.compare(regionScoreA, regionScoreB);
        }

        int overlapA = calculateOverlap(a.moimCode(), moimCategoryMap, preferredCategorySet);
        int overlapB = calculateOverlap(b.moimCode(), moimCategoryMap, preferredCategorySet);
        if (overlapA != overlapB) {
            return Integer.compare(overlapB, overlapA);
        }

        int dateCompare = a.date().compareTo(b.date());
        if (dateCompare != 0) return dateCompare;

        return b.regDt().compareTo(a.regDt());
    }

    private int calculateOverlap(
            String moimCode,
            Map<String, Set<String>> moimCategoryMap,
            Set<String> preferredCategorySet
    ) {
        Set<String> categories = moimCategoryMap.getOrDefault(moimCode, Set.of());

        int count = 0;
        for (String categoryCode : categories) {
            if (preferredCategorySet.contains(categoryCode)) {
                count++;
            }
        }
        return count;
    }

    private int normalizeLimit(int limit, int defaultLimit) {
        if (limit <= 0) return defaultLimit;
        return Math.min(limit, LIMIT_MAX);
    }

    // 모임 상세 조회
    @Override
    public MoimDetailResult getMoimDetail(String moimCode, String memberCode) {

        moimAccessSupportService.getAccessibleMoim(moimCode, memberCode);

        MoimDetailQuery detail = moimQueryRepository.findMoimDetail(moimCode);

        if (detail == null) {
            throw new BusinessException(MoimErrorCode.MOIM_NOT_FOUND);
        }

        List<MoimDetailImageResult> images = fileQueryRepository.findDetails(FileType.M, moimCode)
                .stream()
                .map(this::toImageResult)
                .toList();

        boolean liked = likeRepository.existsByMemberCodeAndTargetCodeAndType(
                memberCode,
                moimCode,
                LikeType.M
        );

        long likeCount = likeRepository.countByTargetCodeAndType(moimCode, LikeType.M);

        long participantCountLong =
                moimMemberMappingRepository.countByIdMoimCodeAndIsApprovedTrueAndJoinYn(
                        moimCode,
                        YesNo.YES
                );

        int participantCount = Math.toIntExact(participantCountLong);
        int extraParticipantCount = Math.max(0, participantCount - 1);

        boolean owner = memberCode.equals(detail.leaderMemberCode());

        return new MoimDetailResult(
                detail.moimCode(),
                detail.title(),
                detail.content(),
                detail.moimDate(),
                detail.maxParticipants(),
                detail.leaderMemberCode(),
                detail.leaderProfileUrl(),
                images,
                likeCount,
                liked,
                participantCount,
                extraParticipantCount,
                owner
        );
    }

    private MoimDetailImageResult toImageResult(FileDetailQuery query) {
        return new MoimDetailImageResult(
                query.fileSeq(),
                query.url()
        );
    }

    // 모임 필터링 조회
    @Override
    public MoimFilterSliceResult getMoimsByFilter(MoimFilterCommand command) {
        Slice<MoimFilterItemQuery> slice = moimFilterQueryRepository.searchMoimsByFilter(command);

        // HOT / FAVORITE 폴백
        if (slice.isEmpty() && shouldFallbackToNew(command)) {
            MoimFilterCommand fallbackCommand = MoimFilterCommand.builder()
                    .memberCode(command.getMemberCode())
                    .viewType(MoimViewType.NEW)
                    .sido(command.getSido())
                    .sigungu(command.getSigungu())
                    .date(command.getDate())
                    .categoryCodes(command.getCategoryCodes())
                    .page(command.getPage())
                    .size(command.getSize())
                    .build();

            slice = moimFilterQueryRepository.searchMoimsByFilter(fallbackCommand);
        }

        if (slice.isEmpty()) {
            return new MoimFilterSliceResult(
                    List.of(),
                    command.getPage(),
                    command.getSize(),
                    false
            );
        }

        List<MoimFilterItemQuery> items = slice.getContent();

        List<String> moimCodes = items.stream()
                .map(MoimFilterItemQuery::moimCode)
                .toList();

        Set<String> likedSet = getLikedMoimCodes(command.getMemberCode(), moimCodes);
        Map<String, List<String>> categoryMap = moimCardSupportService.getMoimCategories(moimCodes);
        Map<String, Integer> participantMap = moimCardSupportService.getParticipantCounts(moimCodes);
        Map<String, List<String>> profileMap = moimCardSupportService.getParticipantProfileUrls(moimCodes);

        Map<String, String> thumbMap = fileThumbnailService.getMoimThumbsWithPopupFallback(
                toThumbTargetsForFilter(items)
        );

        List<MoimFilterResult> results = items.stream()
                .map(item -> {
                    int participantCount = participantMap.getOrDefault(item.moimCode(), 0);
                    int maxParticipantCount = item.maxParticipantCount() != null ? item.maxParticipantCount() : 0;
                    boolean isFull = participantCount >= maxParticipantCount;

                    return new MoimFilterResult(
                            item.moimCode(),
                            thumbMap.get(item.moimCode()),
                            item.title(),
                            categoryMap.getOrDefault(item.moimCode(), List.of()),
                            item.address(),
                            item.moimDate(),
                            item.leaderNickname(),
                            participantCount,
                            maxParticipantCount,
                            isFull,
                            likedSet.contains(item.moimCode()),
                            profileMap.getOrDefault(item.moimCode(), List.of())
                    );
                })
                .toList();

        return new MoimFilterSliceResult(
                results,
                slice.getNumber(),
                slice.getSize(),
                slice.hasNext()
        );
    }

    private boolean shouldFallbackToNew(MoimFilterCommand command) {
        return command.getViewType() == MoimViewType.HOT
                || command.getViewType() == MoimViewType.FAVORITE;
    }

    private Set<String> getLikedMoimCodes(String memberCode, List<String> moimCodes) {
        if (moimCodes == null || moimCodes.isEmpty()) {
            return Set.of();
        }
        return moimCardSupportService.getLikedMoimCodes(memberCode, moimCodes);
    }

    private List<MoimThumbTarget> toThumbTargetsForFilter(List<MoimFilterItemQuery> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        return items.stream()
                .map(item -> new MoimThumbTarget(item.moimCode(), item.popupCode()))
                .toList();
    }
}