package com.spoons.popparazzi.moim.service;

import com.spoons.popparazzi.category.dto.query.MoimCategoryRow;
import com.spoons.popparazzi.category.repository.CategoryQueryRepository;
import com.spoons.popparazzi.error.exception.BusinessException;
import com.spoons.popparazzi.file.dto.query.FileThumbQuery;
import com.spoons.popparazzi.file.entity.FileType;
import com.spoons.popparazzi.file.repository.FileThumbQueryRepository;
import com.spoons.popparazzi.like.entity.LikeType;
import com.spoons.popparazzi.like.repository.LikeQueryRepository;
import com.spoons.popparazzi.moim.dto.command.CreateMoimCommand;
import com.spoons.popparazzi.moim.dto.query.HotMoimCardQuery;
import com.spoons.popparazzi.moim.dto.query.HotMoimRankQuery;
import com.spoons.popparazzi.moim.dto.query.NewestMoimCardQuery;
import com.spoons.popparazzi.moim.dto.query.NewestMoimItemQuery;
import com.spoons.popparazzi.moim.error.MoimErrorCode;
import com.spoons.popparazzi.moim.repository.HotMoimQueryRepository;
import com.spoons.popparazzi.moim.repository.MoimQueryRepository;
import com.spoons.popparazzi.moim.repository.MoimRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class MoimServiceImpl implements MoimService {

    private final MoimRepository moimRepository;
    private final MoimQueryRepository moimQueryRepository;

    private final HotMoimQueryRepository hotMoimQueryRepository;
    private final FileThumbQueryRepository fileThumbQueryRepository;

    private final LikeQueryRepository likeQueryRepository;
    private final CategoryQueryRepository categoryQueryRepository;

    @Override
    public Long create(CreateMoimCommand command) {

        if (command == null) {
            throw new BusinessException(MoimErrorCode.MOIM_NOT_FOUND);
        }
        return null;
    }

    // 신규 모임 카드
    @Override
    public List<NewestMoimCardQuery> getNewestMoimsForMain(int limit, String memberCode) {

        if (limit <= 0) limit = 3;
        if (limit > 50) limit = 50;

        var items = moimQueryRepository.findNewestForMain(PageRequest.of(0, limit));

        if (items.isEmpty()) return List.of();

        var moimCodes = items.stream().map(NewestMoimItemQuery::moimCode).toList();
        var popupCodes = items.stream().map(NewestMoimItemQuery::popupCode).toList();

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

        // 3️⃣ 썸네일 (모임 → 없으면 팝업)
        var moimThumbs = fileThumbQueryRepository.findFirstThumbs(FileType.M, moimCodes)
                .stream()
                .collect(Collectors.toMap(FileThumbQuery::parentCode, FileThumbQuery::url));

        var popupThumbs = fileThumbQueryRepository.findFirstThumbs(FileType.P, popupCodes)
                .stream()
                .collect(Collectors.toMap(FileThumbQuery::parentCode, FileThumbQuery::url));

        // 4️⃣ 카드 조합
        return items.stream()
                .map(it -> {

                    String thumb = moimThumbs.getOrDefault(
                            it.moimCode(),
                            popupThumbs.get(it.popupCode())
                    );

                    List<String> categories =
                            categoryMap.getOrDefault(it.moimCode(), List.of())
                                    .stream()
                                    .limit(3)
                                    .toList();

                    return new NewestMoimCardQuery(
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
    public List<HotMoimCardQuery> getHotMoimCardsForMain(int limit) {
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
        var baseMap = bases.stream().collect(java.util.stream.Collectors.toMap(
                HotMoimCardQuery::moimCode,
                it -> it
        ));

        // 3) 모임 썸네일 우선
        var moimThumbMap = fileThumbQueryRepository.findFirstThumbs(FileType.M, mmCodes).stream()
                .collect(java.util.stream.Collectors.toMap(
                        FileThumbQuery::parentCode,
                        FileThumbQuery::url
                ));

        // 4) 모임썸네일 없는 경우 팝업 썸네일 fallback 준비
        var needPopupPmCodes = bases.stream()
                .filter(b -> !moimThumbMap.containsKey(b.moimCode()))
                .map(HotMoimCardQuery::popupCode)
                .distinct()
                .toList();

        java.util.Map<String, String> popupThumbMap = new java.util.HashMap<>();
        if (!needPopupPmCodes.isEmpty()) {
            var popupThumbs = fileThumbQueryRepository.findFirstThumbs(FileType.P, needPopupPmCodes);
            for (var t : popupThumbs) {
                popupThumbMap.put(t.parentCode(), t.url());
            }
        }

        // 5) 랭크 순서 유지해서 최종 조립 (likeCount는 r에서 바로 씀)
        return ranks.stream()
                .map(r -> {
                    var b = baseMap.get(r.moimCode());
                    if (b == null) return null;

                    var thumb = moimThumbMap.get(b.moimCode());
                    if (thumb == null) thumb = popupThumbMap.get(b.popupCode());

                    return new HotMoimCardQuery(
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
                .filter(java.util.Objects::nonNull)
                .toList();
    }

}
