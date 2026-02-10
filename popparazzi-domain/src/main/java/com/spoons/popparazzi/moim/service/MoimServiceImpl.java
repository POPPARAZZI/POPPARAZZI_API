package com.spoons.popparazzi.moim.service;

import com.spoons.popparazzi.error.exception.BusinessException;
import com.spoons.popparazzi.file.entity.FileType;
import com.spoons.popparazzi.like.entity.LikeType;
import com.spoons.popparazzi.moim.dto.command.CreateMoimCommand;
import com.spoons.popparazzi.moim.dto.query.FileThumbQuery;
import com.spoons.popparazzi.moim.dto.query.HotMoimCardQuery;
import com.spoons.popparazzi.moim.dto.query.HotMoimRankQuery;
import com.spoons.popparazzi.moim.dto.query.NewestMoimItemQuery;
import com.spoons.popparazzi.moim.error.MoimErrorCode;
import com.spoons.popparazzi.moim.repository.HotMoimQueryRepository;
import com.spoons.popparazzi.moim.repository.MoimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class MoimServiceImpl implements MoimService {

    private final MoimRepository moimRepository;
    private final HotMoimQueryRepository hotMoimQueryRepository;

    @Override
    public Long create(CreateMoimCommand command) {

        if (command == null) {
            throw new BusinessException(MoimErrorCode.MOIM_NOT_FOUND);
        }
        return null;
    }

    // 신규 모임 카드
    @Override
    public List<NewestMoimItemQuery> getNewestMoimsForMain() {
        return moimRepository.findNewestForMain(
                PageRequest.of(0, 3)
        );
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

        var mmCodes = ranks.stream().map(HotMoimRankQuery::mmCode).toList();

        // 2) 카드 기본 정보
        var bases = hotMoimQueryRepository.findHotCardsBase(mmCodes);
        var baseMap = bases.stream().collect(java.util.stream.Collectors.toMap(
                HotMoimCardQuery::mmCode,
                it -> it
        ));

        // 3) 모임 썸네일 우선
        var moimThumbMap = hotMoimQueryRepository.findFirstThumbs(FileType.M, mmCodes).stream()
                .collect(java.util.stream.Collectors.toMap(
                        FileThumbQuery::parentCode,
                        FileThumbQuery::url
                ));

        // 4) 모임썸네일 없는 경우 팝업 썸네일 fallback 준비
        var needPopupPmCodes = bases.stream()
                .filter(b -> !moimThumbMap.containsKey(b.mmCode()))
                .map(HotMoimCardQuery::pmCode)
                .distinct()
                .toList();

        java.util.Map<String, String> popupThumbMap = new java.util.HashMap<>();
        if (!needPopupPmCodes.isEmpty()) {
            var popupThumbs = hotMoimQueryRepository.findFirstThumbs(FileType.P, needPopupPmCodes);
            for (var t : popupThumbs) {
                popupThumbMap.put(t.parentCode(), t.url());
            }
        }

        // 5) 랭크 순서 유지해서 최종 조립 (likeCount는 r에서 바로 씀)
        return ranks.stream()
                .map(r -> {
                    var b = baseMap.get(r.mmCode());
                    if (b == null) return null;

                    var thumb = moimThumbMap.get(b.mmCode());
                    if (thumb == null) thumb = popupThumbMap.get(b.pmCode());

                    return new HotMoimCardQuery(
                            b.mmCode(),
                            b.pmCode(),
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
