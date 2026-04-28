package com.spoons.popparazzi.moim.service;

import com.spoons.popparazzi.error.exception.BusinessException;
import com.spoons.popparazzi.file.dto.query.MoimThumbTarget;
import com.spoons.popparazzi.file.enums.FileType;
import com.spoons.popparazzi.file.repository.FileQueryRepository;
import com.spoons.popparazzi.file.service.FileThumbnailService;
import com.spoons.popparazzi.like.dto.query.LikeRankQuery;
import com.spoons.popparazzi.like.enums.LikeType;
import com.spoons.popparazzi.like.repository.LikeQueryRepository;
import com.spoons.popparazzi.like.repository.LikeRepository;
import com.spoons.popparazzi.moim.dto.command.MoimFilterCommand;
import com.spoons.popparazzi.moim.dto.command.MoimSearchCommand;
import com.spoons.popparazzi.moim.dto.query.MoimFilterItemQuery;
import com.spoons.popparazzi.moim.dto.query.MoimSearchItemQuery;
import com.spoons.popparazzi.moim.dto.query.main.*;
import com.spoons.popparazzi.moim.dto.result.*;
import com.spoons.popparazzi.moim.enums.MoimMemberStatus;
import com.spoons.popparazzi.moim.enums.MoimViewType;
import com.spoons.popparazzi.moim.error.MoimErrorCode;
import com.spoons.popparazzi.moim.repository.MoimMemberMappingRepository;
import com.spoons.popparazzi.moim.repository.MoimQueryRepository;
import com.spoons.popparazzi.moim.service.support.MoimAccessSupportService;
import com.spoons.popparazzi.moim.service.support.MoimCardSupportService;
import com.spoons.popparazzi.popup.dto.command.PopupSearchMatchCommand;
import com.spoons.popparazzi.popup.dto.result.PopupSearchMatchResult;
import com.spoons.popparazzi.popup.service.PopupSearchService;
import com.spoons.popparazzi.util.PaginationInfo;
import com.spoons.popparazzi.util.SearchKeywordNormalizer;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MoimQueryServiceImpl implements MoimQueryService {

    private static final int NEWEST_DEFAULT_LIMIT = 3;
    private static final int HOT_DEFAULT_LIMIT = 10;
    private static final int LIMIT_MAX = 50;
    private static final int SEARCH_DEFAULT_PAGE = 1;
    private static final int SEARCH_DEFAULT_SIZE = 10;

    private final MoimQueryRepository moimQueryRepository;
    private final MoimMemberMappingRepository moimMemberMappingRepository;
    private final FileQueryRepository fileQueryRepository;
    private final LikeRepository likeRepository;
    private final LikeQueryRepository likeQueryRepository;
    private final FileThumbnailService fileThumbnailService;
    private final MoimCardSupportService moimCardSupportService;
    private final MoimAccessSupportService moimAccessSupportService;
    private final PopupSearchService popupSearchService;

    // ─────────────────────────────────────────────────────────────────────────
    // 메인 화면
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public List<NewestMoimCardResult> getNewestMoimsForMain(int limit, String memberCode) {
        int fixedLimit = normalizeLimit(limit, NEWEST_DEFAULT_LIMIT);

        List<NewestMoimItemQuery> items =
                moimQueryRepository.findNewestForMain(PageRequest.of(0, fixedLimit));

        if (items.isEmpty()) return List.of();

        return buildNewestCards(items, memberCode);
    }

    @Override
    public List<HotMoimCardResult> getHotMoimCardsForMain(int limit) {
        int fixedLimit = normalizeLimit(limit, HOT_DEFAULT_LIMIT);
        LocalDateTime since = LocalDate.now().atStartOfDay();

        List<LikeRankQuery> ranks = likeQueryRepository.findTopRankKeys(
                LikeType.M, since, PageRequest.of(0, fixedLimit)
        );

        if (ranks.isEmpty()) return buildLatestHotFallback(fixedLimit);

        List<HotMoimCardResult> hotCards = buildHotCardsFromRanks(ranks);

        return hotCards.isEmpty() ? buildLatestHotFallback(fixedLimit) : hotCards;
    }

    @Override
    public List<MoimRecommendCardResult> recommendForMember(String memberCode) {
        List<String> sigunguPriority = moimQueryRepository
                .findPreferredSigunguTop(memberCode, 30, 3)
                .stream()
                .map(PreferredSigunguQuery::sigungu)
                .toList();

        if (sigunguPriority.isEmpty()) return buildLatestRecommendFallback(memberCode, 10);

        List<RecommendedMoimBaseQuery> candidates =
                moimQueryRepository.findRecommendMoimCandidates(sigunguPriority, memberCode, 20);

        if (candidates.isEmpty()) return buildLatestRecommendFallback(memberCode, 10);

        return buildRecommendCards(memberCode, sigunguPriority, candidates);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 상세
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public MoimDetailResult getMoimDetail(String moimCode, String memberCode) {
        moimAccessSupportService.getAccessibleMoim(moimCode, memberCode);

        var detail = moimQueryRepository.findMoimDetail(moimCode);
        if (detail == null) throw new BusinessException(MoimErrorCode.MOIM_NOT_FOUND);

        List<MoimDetailImageResult> images = fileQueryRepository.findDetails(FileType.M, moimCode)
                .stream()
                .map(MoimDetailImageResult::from)   // ← from() 적용
                .toList();

        boolean liked = likeRepository.existsByMemberCodeAndTargetCodeAndType(
                memberCode, moimCode, LikeType.M
        );
        long likeCount = likeRepository.countByTargetCodeAndType(moimCode, LikeType.M);

        int participantCount = Math.toIntExact(
                moimMemberMappingRepository.countByIdMoimCodeAndStatus(moimCode, MoimMemberStatus.APPROVED)
        );

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
                Math.max(0, participantCount - 1),
                memberCode.equals(detail.leaderMemberCode())
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 필터
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public MoimFilterSliceResult getMoimsByFilter(MoimFilterCommand command) {
        Slice<MoimFilterItemQuery> slice = moimQueryRepository.searchMoimsByFilter(command);

        if (slice.isEmpty() && shouldFallbackToNew(command)) {
            slice = moimQueryRepository.searchMoimsByFilter(toNewFallbackCommand(command));
        }

        if (slice.isEmpty()) {
            return new MoimFilterSliceResult(List.of(), command.getPage(), command.getSize(), false);
        }

        List<MoimFilterItemQuery> items = slice.getContent();
        List<String> moimCodes = items.stream().map(MoimFilterItemQuery::moimCode).toList();

        Set<String> likedSet = moimCardSupportService.getLikedMoimCodes(command.getMemberCode(), moimCodes);
        Map<String, List<String>> categoryMap = moimCardSupportService.getMoimCategories(moimCodes);
        Map<String, Integer> participantMap = moimCardSupportService.getParticipantCounts(moimCodes);
        Map<String, List<String>> profileMap = moimCardSupportService.getParticipantProfileUrls(moimCodes);
        Map<String, String> thumbMap = fileThumbnailService.getMoimThumbsWithPopupFallback(
                toThumbTargets(items, MoimFilterItemQuery::moimCode, MoimFilterItemQuery::popupCode)
        );

        List<MoimFilterResult> results = items.stream()
                .map(item -> {
                    int participantCount = participantMap.getOrDefault(item.moimCode(), 0);
                    int maxCount = item.maxParticipantCount() != null ? item.maxParticipantCount() : 0;

                    return new MoimFilterResult(
                            item.moimCode(),
                            thumbMap.get(item.moimCode()),
                            item.title(),
                            categoryMap.getOrDefault(item.moimCode(), List.of()),
                            item.address(),
                            item.moimDate(),
                            item.leaderNickname(),
                            participantCount,
                            maxCount,
                            participantCount >= maxCount,
                            likedSet.contains(item.moimCode()),
                            profileMap.getOrDefault(item.moimCode(), List.of())
                    );
                })
                .toList();

        return new MoimFilterSliceResult(results, slice.getNumber(), slice.getSize(), slice.hasNext());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 검색
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public MoimSearchResult searchMoims(MoimSearchCommand command) {
        validateKeyword(command.keyword());

        String normalizedKeyword = SearchKeywordNormalizer.normalize(command.keyword());
        PaginationInfo paginationInfo = resolvedPagination(command.paginationInfo());

        PopupSearchMatchResult matchedPopup = popupSearchService.findBestMatch(
                new PopupSearchMatchCommand(command.keyword())
        );

        long totalCount = moimQueryRepository.countSearchMoims(normalizedKeyword);
        paginationInfo.setTotalRecord(Math.toIntExact(totalCount));
        paginationInfo.pageInit();

        if (totalCount == 0) {
            return emptySearchResult(command.keyword(), matchedPopup, paginationInfo);
        }

        List<MoimSearchItemQuery> items = moimQueryRepository.searchMoims(normalizedKeyword, paginationInfo);
        if (items.isEmpty()) {
            return emptySearchResult(command.keyword(), matchedPopup, paginationInfo);
        }

        List<String> moimCodes = items.stream().map(MoimSearchItemQuery::moimCode).toList();

        Set<String> likedSet = moimCardSupportService.getLikedMoimCodes(command.memberCode(), moimCodes);
        Map<String, List<String>> categoryMap = moimCardSupportService.getMoimCategories(moimCodes);
        Map<String, Integer> participantCountMap = moimCardSupportService.getParticipantCounts(moimCodes);
        Map<String, List<String>> profileMap = moimCardSupportService.getParticipantProfileUrls(moimCodes);
        Map<String, String> thumbMap = fileThumbnailService.getMoimThumbsWithPopupFallback(
                toThumbTargets(items, MoimSearchItemQuery::moimCode, MoimSearchItemQuery::popupCode)
        );

        List<MoimSearchCardResult> cards = items.stream()
                .map(item -> {
                    int participantCount = participantCountMap.getOrDefault(item.moimCode(), 1);
                    int maxCount = item.maxCount() == null ? 0 : item.maxCount();

                    return new MoimSearchCardResult(
                            item.moimCode(),
                            thumbMap.get(item.moimCode()),
                            likedSet.contains(item.moimCode()),
                            categoryMap.getOrDefault(item.moimCode(), List.of()),
                            item.title(),
                            item.address(),
                            profileMap.getOrDefault(item.moimCode(), List.of()),
                            item.leaderNickname(),
                            participantCount,
                            maxCount,
                            isClosingSoon(participantCount, maxCount)
                    );
                })
                .toList();

        return new MoimSearchResult(
                command.keyword(),
                matchedPopup,
                cards,
                paginationInfo.getCurrentPage(),
                paginationInfo.getRecordCountPerPage(),
                paginationInfo.getTotalRecord(),
                paginationInfo.getTotalPage()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 카드 조립 (private)
    // ─────────────────────────────────────────────────────────────────────────

    private List<NewestMoimCardResult> buildNewestCards(List<NewestMoimItemQuery> items, String memberCode) {
        List<String> moimCodes = items.stream().map(NewestMoimItemQuery::moimCode).toList();

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

    private List<HotMoimCardResult> buildHotCardsFromRanks(List<LikeRankQuery> ranks) {
        List<String> moimCodes = ranks.stream().map(LikeRankQuery::targetCode).toList();

        List<HotMoimCardResult> bases = moimQueryRepository.findHotCardsBase(moimCodes);
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

    private List<HotMoimCardResult> buildLatestHotFallback(int limit) {
        List<NewestMoimItemQuery> latest = moimQueryRepository.findNewestForMain(PageRequest.of(0, limit));
        if (latest.isEmpty()) return List.of();

        List<String> moimCodes = latest.stream().map(NewestMoimItemQuery::moimCode).toList();
        Map<String, Integer> participantMap = moimCardSupportService.getParticipantCounts(moimCodes);
        Map<String, String> thumbMap = fileThumbnailService.getMoimThumbsWithPopupFallback(
                toThumbTargets(latest, NewestMoimItemQuery::moimCode, NewestMoimItemQuery::popupCode)
        );

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

    private List<MoimRecommendCardResult> buildRecommendCards(
            String memberCode,
            List<String> sigunguPriority,
            List<RecommendedMoimBaseQuery> candidates
    ) {
        List<String> moimCodes = candidates.stream().map(RecommendedMoimBaseQuery::moimCode).toList();

        Map<String, Integer> participantMap = moimCardSupportService.getParticipantCounts(moimCodes);
        Set<String> likedSet = moimCardSupportService.getLikedMoimCodes(memberCode, moimCodes);

        Set<String> preferredCategorySet = moimQueryRepository
                .findPreferredCategories(memberCode, 30, 5)
                .stream()
                .map(PreferredCategoryQuery::categoryCode)
                .collect(Collectors.toSet());

        Map<String, Set<String>> moimCategoryMap = moimQueryRepository
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

    private List<MoimRecommendCardResult> buildLatestRecommendFallback(String memberCode, int limit) {
        List<NewestMoimItemQuery> latest = moimQueryRepository.findNewestForMain(PageRequest.of(0, limit));
        if (latest.isEmpty()) return List.of();

        List<String> moimCodes = latest.stream().map(NewestMoimItemQuery::moimCode).toList();
        Set<String> likedSet = moimCardSupportService.getLikedMoimCodes(memberCode, moimCodes);
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

    // ─────────────────────────────────────────────────────────────────────────
    // 내부 유틸
    // ─────────────────────────────────────────────────────────────────────────

    private int compareRecommend(
            RecommendedMoimBaseQuery a,
            RecommendedMoimBaseQuery b,
            List<String> sigunguPriority,
            Map<String, Set<String>> moimCategoryMap,
            Set<String> preferredCategorySet
    ) {
        int regionScoreA = sigunguPriority.indexOf(a.sigungu());
        int regionScoreB = sigunguPriority.indexOf(b.sigungu());
        if (regionScoreA != regionScoreB) return Integer.compare(regionScoreA, regionScoreB);

        int overlapA = countCategoryOverlap(a.moimCode(), moimCategoryMap, preferredCategorySet);
        int overlapB = countCategoryOverlap(b.moimCode(), moimCategoryMap, preferredCategorySet);
        if (overlapA != overlapB) return Integer.compare(overlapB, overlapA);

        int dateCompare = a.date().compareTo(b.date());
        if (dateCompare != 0) return dateCompare;

        return b.regDt().compareTo(a.regDt());
    }

    private int countCategoryOverlap(
            String moimCode,
            Map<String, Set<String>> moimCategoryMap,
            Set<String> preferredCategorySet
    ) {
        Set<String> categories = moimCategoryMap.getOrDefault(moimCode, Set.of());
        int count = 0;
        for (String code : categories) {
            if (preferredCategorySet.contains(code)) count++;
        }
        return count;
    }

    private boolean shouldFallbackToNew(MoimFilterCommand command) {
        return command.getViewType() == MoimViewType.HOT
                || command.getViewType() == MoimViewType.FAVORITE;
    }

    private MoimFilterCommand toNewFallbackCommand(MoimFilterCommand command) {
        return MoimFilterCommand.builder()
                .memberCode(command.getMemberCode())
                .viewType(MoimViewType.NEW)
                .sido(command.getSido())
                .sigungu(command.getSigungu())
                .date(command.getDate())
                .categoryCodes(command.getCategoryCodes())
                .page(command.getPage())
                .size(command.getSize())
                .build();
    }

    private void validateKeyword(String keyword) {
        if (!SearchKeywordNormalizer.isValidLength(keyword)) {
            throw new BusinessException(MoimErrorCode.INVALID_SEARCH_KEYWORD);
        }
    }

    private PaginationInfo resolvedPagination(PaginationInfo paginationInfo) {
        if (paginationInfo != null) return paginationInfo;

        PaginationInfo defaults = new PaginationInfo();
        defaults.setCurrentPage(SEARCH_DEFAULT_PAGE);
        defaults.setRecordCountPerPage(SEARCH_DEFAULT_SIZE);
        return defaults;
    }

    private boolean isClosingSoon(int currentCount, int maxCount) {
        if (maxCount <= 0) return false;
        int remain = maxCount - currentCount;
        return maxCount <= 5 ? remain == 1 : remain <= 2;
    }

    private <T> List<MoimThumbTarget> toThumbTargets(
            List<T> items,
            Function<T, String> moimCodeGetter,
            Function<T, String> popupCodeGetter
    ) {
        if (items == null || items.isEmpty()) return List.of();

        return items.stream()
                .map(it -> new MoimThumbTarget(moimCodeGetter.apply(it), popupCodeGetter.apply(it)))
                .toList();
    }

    private MoimSearchResult emptySearchResult(
            String keyword,
            PopupSearchMatchResult matchedPopup,
            PaginationInfo paginationInfo
    ) {
        return new MoimSearchResult(
                keyword,
                matchedPopup,
                List.of(),
                paginationInfo.getCurrentPage(),
                paginationInfo.getRecordCountPerPage(),
                paginationInfo.getTotalRecord(),
                paginationInfo.getTotalPage()
        );
    }

    private int normalizeLimit(int limit, int defaultLimit) {
        if (limit <= 0) return defaultLimit;
        return Math.min(limit, LIMIT_MAX);
    }
}
