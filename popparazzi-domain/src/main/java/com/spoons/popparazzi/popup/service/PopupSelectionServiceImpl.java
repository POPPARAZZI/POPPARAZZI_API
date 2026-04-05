package com.spoons.popparazzi.popup.service;

import com.spoons.popparazzi.file.dto.query.FileThumbQuery;
import com.spoons.popparazzi.file.enums.FileType;
import com.spoons.popparazzi.file.repository.FileThumbQueryRepository;
import com.spoons.popparazzi.like.enums.LikeType;
import com.spoons.popparazzi.like.repository.LikeQueryRepository;
import com.spoons.popparazzi.popup.dto.query.PopupSelectionItemQuery;
import com.spoons.popparazzi.popup.dto.result.PopupSelectionItemResult;
import com.spoons.popparazzi.popup.repository.PopupSelectionQueryRepository;
import com.spoons.popparazzi.popup.repository.PopupViewHistoryQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopupSelectionServiceImpl implements PopupSelectionService {

    private final PopupSelectionQueryRepository popupSelectionQueryRepository;
    private final PopupViewHistoryQueryRepository popupViewHistoryQueryRepository;
    private final FileThumbQueryRepository fileThumbQueryRepository;
    private final LikeQueryRepository likeQueryRepository;

    @Override
    public List<PopupSelectionItemResult> getNewestSelectionItems(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }

        List<PopupSelectionItemQuery> queries = popupSelectionQueryRepository.findNewestSelections(limit);

        if (queries.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> popupCodes = queries.stream()
                .map(PopupSelectionItemQuery::popupCode)
                .toList();

        Map<String, String> thumbnailMap = getPopupThumbnailMap(popupCodes);
        Map<String, Long> likeCountMap = likeQueryRepository.countTargetsByType(LikeType.P, popupCodes);
        Map<String, Long> viewCountMap = popupViewHistoryQueryRepository.countViewsByPopupCodes(popupCodes);

        return queries.stream()
                .map(query -> toResult(query, thumbnailMap, likeCountMap, viewCountMap))
                .toList();
    }

    private PopupSelectionItemResult toResult(
            PopupSelectionItemQuery query,
            Map<String, String> thumbnailMap,
            Map<String, Long> likeCountMap,
            Map<String, Long> viewCountMap
    ) {
        String popupCode = query.popupCode();

        return new PopupSelectionItemResult(
                query.popupCode(),
                query.title(),
                query.sido(),
                query.sigungu(),
                query.startDt(),
                query.endDt(),
                likeCountMap.getOrDefault(popupCode, 0L),
                viewCountMap.getOrDefault(popupCode, 0L),
                thumbnailMap.get(popupCode)
        );
    }

    private Map<String, String> getPopupThumbnailMap(List<String> popupCodes) {
        List<FileThumbQuery> thumbnails = fileThumbQueryRepository.findFirstThumbs(FileType.P, popupCodes);

        if (thumbnails == null || thumbnails.isEmpty()) {
            return Collections.emptyMap();
        }

        return thumbnails.stream()
                .collect(Collectors.toMap(
                        FileThumbQuery::parentCode,
                        FileThumbQuery::url
                ));
    }
}