package com.spoons.popparazzi.moim.service;

import com.spoons.popparazzi.category.dto.query.MoimCategoryRow;
import com.spoons.popparazzi.category.repository.CategoryQueryRepository;
import com.spoons.popparazzi.error.exception.BusinessException;
import com.spoons.popparazzi.file.dto.query.MoimThumbTarget;
import com.spoons.popparazzi.file.service.FileThumbnailService;
import com.spoons.popparazzi.like.enums.LikeType;
import com.spoons.popparazzi.like.repository.LikeQueryRepository;
import com.spoons.popparazzi.moim.dto.command.CreateMoimCommand;
import com.spoons.popparazzi.moim.dto.query.hot.HotMoimRankQuery;
import com.spoons.popparazzi.moim.dto.query.newest.NewestMoimItemQuery;
import com.spoons.popparazzi.moim.dto.query.recommend.*;
import com.spoons.popparazzi.moim.dto.result.HotMoimCardResult;
import com.spoons.popparazzi.moim.dto.result.MoimRecommendCardResult;
import com.spoons.popparazzi.moim.dto.result.NewestMoimCardResult;
import com.spoons.popparazzi.moim.error.MoimErrorCode;
import com.spoons.popparazzi.moim.repository.MoimRepository;
import com.spoons.popparazzi.moim.repository.hot.HotMoimQueryRepository;
import com.spoons.popparazzi.moim.repository.newest.MoimQueryRepository;
import com.spoons.popparazzi.moim.repository.recommend.MoimRecommendQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class MoimServiceImpl implements MoimService {

    private final MoimRepository moimRepository;
    private final MoimQueryRepository moimQueryRepository;
    private final HotMoimQueryRepository hotMoimQueryRepository;
    private final MoimRecommendQueryRepository recommendQueryRepository;
    private final LikeQueryRepository likeQueryRepository;
    private final CategoryQueryRepository categoryQueryRepository;

    // ✅ 썸네일 공통 서비스로 통일
    private final FileThumbnailService fileThumbnailService;

    @Override
    public Long create(CreateMoimCommand command) {

        if (command == null) {
            throw new BusinessException(MoimErrorCode.MOIM_NOT_FOUND);
        }
        return null;
    }

    // 신규 모임 카드
    @Override
    public List<NewestMoimCardResult> getNewestMoimsForMain(int limit, String memberCode) {

        if (limit <= 0) limit = 3;
        if (limit > 50) limit = 50;

        var items = moimQueryRepository.findNewestForMain(PageRequest.of(0, limit));
        if (items.isEmpty()) return List.of();

        var moimCodes = items.stream().map(NewestMoimItemQuery::moimCode).toList();

        // 1️⃣ 좋아요
        var likedSet = new HashSet<>(
                likeQueryRepository.findLikedMoimCodes(memberCode, moimCodes)
        );

        // 2️⃣ 카테고리
        var categoryRows = categoryQueryRepository.findMoimCategories(moimCodes);

        Map<String, List<String>> categoryMap =
                categoryRows.stream()
                        .collect(Collectors.groupingBy(
                                MoimCategoryRow::parentCode,
                                Collectors.mapping(
                                        MoimCategoryRow::categoryName,
                                        Collectors.toList()
                                )
                        ));

        // 3️⃣ 썸네일 (모임 → 없으면 팝업) ✅ 공통 서비스 사용
        List<MoimThumbTarget> targets = items.stream()
                .map(it -> new MoimThumbTarget(it.moimCode(), it.popupCode()))
                .toList();

        Map<String, String> thumbMap = fileThumbnailService.getMoimThumbsWithPopupFallback(targets);

        // 4️⃣ 카드 조합
        return items.stream()
                .map(it -> {

                    String thumb = thumbMap.get(it.moimCode());

                    List<String> categories =
                            categoryMap.getOrDefault(it.moimCode(), List.of())
                                    .stream()
                                    .limit(3)
                                    .toList();

                    return new NewestMoimCardResult(
                            it.moimCode(),
                            it.popupCode(),
                            it.title(),
                            it.date(),
                            it.maxParticipants(),
                            thumb,
                            likedSet.contains(it.moimCode()),
                            categories
                    );
                })
                .toList();
    }


    // 핫한 모임 10위 조회
    @Override
    public List<HotMoimRankQuery> getHotMoimsRanks(int limit) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);

        return hotMoimQueryRepository.findHotRankKeys(
                LikeType.M,
                since,
                org.springframework.data.domain.PageRequest.of(0, limit)
        );
    }

    // 핫한 모임 조회
    @Override
    public List<HotMoimCardResult> getHotMoimCardsForMain(int limit) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);

        // 1) 랭크 TopN
        var ranks = hotMoimQueryRepository.findHotRankKeys(
                LikeType.M,
                since,
                PageRequest.of(0, limit)
        );
        if (ranks.isEmpty()) return List.of();

        var mmCodes = ranks.stream().map(HotMoimRankQuery::moimCode).toList();

        // 2) 카드 기본 정보
        var bases = hotMoimQueryRepository.findHotCardsBase(mmCodes);
        var baseMap = bases.stream().collect(Collectors.toMap(
                HotMoimCardResult::moimCode,
                it -> it
        ));

        // 3) 썸네일 (모임 → 없으면 팝업) ✅ 공통 서비스 사용
        List<MoimThumbTarget> targets = bases.stream()
                .map(b -> new MoimThumbTarget(b.moimCode(), b.popupCode()))
                .toList();

        Map<String, String> thumbMap = fileThumbnailService.getMoimThumbsWithPopupFallback(targets);

        // 4) 랭크 순서 유지해서 최종 조립 (likeCount는 r에서 바로 씀)
        return ranks.stream()
                .map(r -> {
                    var b = baseMap.get(r.moimCode());
                    if (b == null) return null;

                    var thumb = thumbMap.get(b.moimCode());

                    return new HotMoimCardResult(
                            b.moimCode(),
                            b.popupCode(),
                            b.title(),
                            b.date(),
                            b.currentParticipants(),
                            b.maxParticipants(),
                            thumb,
                            r.likeCount24h()
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }

    // 즐겨찾기 기반 모임 조회
    @Override
    public List<MoimRecommendCardResult> recommendForMember(String memberCode) {

        // 1️⃣ 선호 지역 Top3
        List<PreferredSigunguQuery> preferredSigungu =
                recommendQueryRepository.findPreferredSigunguTop(memberCode, 30, 3);

        List<String> sigunguPriority = preferredSigungu.stream()
                .map(PreferredSigunguQuery::sigungu)
                .toList();

        // 2️⃣ 1차 후보 모임 조회
        List<RecommendedMoimBaseQuery> candidates = sigunguPriority.isEmpty()
                ? List.of()
                : recommendQueryRepository.findRecommendMoimCandidates(sigunguPriority, memberCode, 20);

        // 후보가 아예 없으면 최신 fallback
        if (candidates.isEmpty()) {
            return buildLatestFallback(memberCode);
        }

        // 3️⃣ 유저 선호 카테고리 TopN
        List<PreferredCategoryQuery> preferredCategories =
                recommendQueryRepository.findPreferredCategories(memberCode, 30, 5);

        Set<String> preferredCategorySet = preferredCategories.stream()
                .map(PreferredCategoryQuery::categoryCode)
                .collect(Collectors.toSet());

        // 4️⃣ 후보 모임 코드 목록
        List<String> moimCodes = candidates.stream()
                .map(RecommendedMoimBaseQuery::moimCode)
                .toList();

        // 5️⃣ 후보 모임 카테고리 조회
        List<MoimCategoryLinkQuery> moimCategories =
                recommendQueryRepository.findMoimCategories(moimCodes);

        Map<String, Set<String>> moimCategoryMap = moimCategories.stream()
                .collect(Collectors.groupingBy(
                        MoimCategoryLinkQuery::moimCode,
                        Collectors.mapping(MoimCategoryLinkQuery::categoryCode, Collectors.toSet())
                ));

        // 6️⃣ 현재 인원 집계
        List<MoimParticipantsCountQuery> participantCounts =
                recommendQueryRepository.countApprovedParticipants(moimCodes);

        Map<String, Long> participantCountMap = participantCounts.stream()
                .collect(Collectors.toMap(
                        MoimParticipantsCountQuery::moimCode,
                        MoimParticipantsCountQuery::participantsCount
                ));

        // 7️⃣ 좋아요 여부 조회
        Set<String> likedSet = new HashSet<>(
                likeQueryRepository.findLikedMoimCodes(memberCode, moimCodes)
        );

        // 8️⃣ 추천 정렬
        List<RecommendedMoimBaseQuery> sorted = candidates.stream()
                .sorted((a, b) -> {

                    // 지역 우선순위
                    int regionScoreA = sigunguPriority.indexOf(a.sigungu());
                    int regionScoreB = sigunguPriority.indexOf(b.sigungu());
                    if (regionScoreA != regionScoreB) {
                        return Integer.compare(regionScoreA, regionScoreB);
                    }

                    // 카테고리 겹침 점수
                    int overlapA = calculateOverlap(a.moimCode(), moimCategoryMap, preferredCategorySet);
                    int overlapB = calculateOverlap(b.moimCode(), moimCategoryMap, preferredCategorySet);
                    if (overlapA != overlapB) {
                        return Integer.compare(overlapB, overlapA);
                    }

                    // 날짜 ASC
                    int dateCompare = a.date().compareTo(b.date());
                    if (dateCompare != 0) return dateCompare;

                    // 최신 등록 우선
                    return b.regDt().compareTo(a.regDt());
                })
                .limit(10)
                .toList();

        // 9️⃣ 썸네일 (모임 → 없으면 팝업) ✅ 공통 서비스 사용
        List<MoimThumbTarget> targets = sorted.stream()
                .map(m -> new MoimThumbTarget(m.moimCode(), m.popupCode()))
                .toList();

        Map<String, String> thumbMap = fileThumbnailService.getMoimThumbsWithPopupFallback(targets);

        // 🔟 최종 카드 반환
        return sorted.stream()
                .map(m -> {

                    String thumb = thumbMap.get(m.moimCode());

                    return new MoimRecommendCardResult(
                            m.moimCode(),
                            m.title(),
                            m.date(),
                            participantCountMap.getOrDefault(m.moimCode(), 0L).intValue(),
                            m.maxParticipants(),
                            thumb,
                            likedSet.contains(m.moimCode())
                    );
                })
                .toList();
    }

    // fallback
    private List<MoimRecommendCardResult> buildLatestFallback(String memberCode) {

        var latest = moimQueryRepository.findNewestForMain(PageRequest.of(0, 10));
        if (latest.isEmpty()) return List.of();

        List<String> moimCodes = latest.stream()
                .map(NewestMoimItemQuery::moimCode)
                .toList();

        Set<String> likedSet = new HashSet<>(
                likeQueryRepository.findLikedMoimCodes(memberCode, moimCodes)
        );

        // ✅ 썸네일 (모임 → 없으면 팝업) 공통 서비스 사용
        List<MoimThumbTarget> targets = latest.stream()
                .map(it -> new MoimThumbTarget(it.moimCode(), it.popupCode()))
                .toList();

        Map<String, String> thumbMap = fileThumbnailService.getMoimThumbsWithPopupFallback(targets);

        return latest.stream()
                .map(it -> {

                    String thumb = thumbMap.get(it.moimCode());

                    return new MoimRecommendCardResult(
                            it.moimCode(),
                            it.title(),
                            it.date(),
                            0,
                            it.maxParticipants(),
                            thumb,
                            likedSet.contains(it.moimCode())
                    );
                })
                .toList();
    }

    private List<MoimRecommendCardResult> mergeFinal(
            List<RecommendedMoimBaseQuery> sorted,
            List<MoimRecommendCardResult> additional,
            Map<String, Long> participantCountMap,
            Set<String> likedSet
    ) {

        List<MoimRecommendCardResult> result = new ArrayList<>();

        for (var m : sorted) {
            result.add(new MoimRecommendCardResult(
                    m.moimCode(),
                    m.title(),
                    m.date(),
                    participantCountMap.getOrDefault(m.moimCode(), 0L).intValue(),
                    m.maxParticipants(),
                    null,
                    likedSet.contains(m.moimCode())
            ));
        }

        result.addAll(additional);
        return result;
    }

    private int calculateOverlap(
            String moimCode,
            Map<String, Set<String>> moimCategoryMap,
            Set<String> preferredCategorySet
    ) {
        Set<String> categories = moimCategoryMap.getOrDefault(moimCode, Collections.emptySet());

        int count = 0;
        for (String category : categories) {
            if (preferredCategorySet.contains(category)) {
                count++;
            }
        }
        return count;
    }
}