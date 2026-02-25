package com.spoons.popparazzi.file.service;

import com.spoons.popparazzi.file.dto.query.MoimThumbTarget;
import com.spoons.popparazzi.file.dto.query.FileThumbQuery;
import com.spoons.popparazzi.file.enums.FileType;
import com.spoons.popparazzi.file.repository.FileThumbQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileThumbnailServiceImpl implements FileThumbnailService {

    private final FileThumbQueryRepository fileThumbQueryRepository;

    // 1. 모임 썸네일 조회 : 모임 -> 팝업
    @Override
    public Map<String, String> getMoimThumbsWithPopupFallback(List<MoimThumbTarget> targets) {

        if (targets == null || targets.isEmpty()) {
            return Map.of();
        }

        // 1) 모임 코드 / 팝업 코드 추출
        List<String> moimCodes = targets.stream()
                .map(MoimThumbTarget::moimCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<String> popupCodes = targets.stream()
                .map(MoimThumbTarget::popupCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (moimCodes.isEmpty()) {
            return Map.of();
        }

        // 2) 모임 썸네일 우선 조회
        Map<String, String> moimThumbMap = fileThumbQueryRepository
                .findFirstThumbs(FileType.M, moimCodes)
                .stream()
                .collect(Collectors.toMap(
                        FileThumbQuery::parentCode,
                        FileThumbQuery::url,
                        (a, b) -> a // 혹시 중복키가 들어오면 첫 값 유지
                ));

        // 3) 모임 썸네일 없는 것만 팝업 썸네일 fallback 조회
        Set<String> needPopupCodes = targets.stream()
                .filter(t -> t.moimCode() != null)
                .filter(t -> !moimThumbMap.containsKey(t.moimCode()))
                .map(MoimThumbTarget::popupCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, String> popupThumbMap = Map.of();
        if (!needPopupCodes.isEmpty()) {
            popupThumbMap = fileThumbQueryRepository
                    .findFirstThumbs(FileType.P, new ArrayList<>(needPopupCodes))
                    .stream()
                    .collect(Collectors.toMap(
                            FileThumbQuery::parentCode,
                            FileThumbQuery::url,
                            (a, b) -> a
                    ));
        }

        // 4) 최종 map: key=moimCode, value=moimThumb or popupThumb
        Map<String, String> result = new HashMap<>();
        for (MoimThumbTarget t : targets) {
            if (t.moimCode() == null) continue;

            String url = moimThumbMap.get(t.moimCode());
            if (url == null && t.popupCode() != null) {
                url = popupThumbMap.get(t.popupCode());
            }

            // url이 null일 수도 있음(둘 다 썸네일 없음) -> 그 상태로 저장
            result.put(t.moimCode(), url);
        }

        return result;
    }
}