package com.spoons.popparazzi.popup.service;

import com.spoons.popparazzi.file.dto.query.FileThumbQuery;
import com.spoons.popparazzi.file.enums.FileType;
import com.spoons.popparazzi.file.repository.FileThumbQueryRepository;
import com.spoons.popparazzi.like.enums.LikeType;
import com.spoons.popparazzi.like.repository.LikeRepository;
import com.spoons.popparazzi.popup.dto.command.PopupSearchMatchCommand;
import com.spoons.popparazzi.popup.dto.query.PopupSearchMatchQuery;
import com.spoons.popparazzi.popup.dto.result.PopupSearchMatchResult;
import com.spoons.popparazzi.popup.repository.PopupSearchQueryRepository;
import com.spoons.popparazzi.popup.repository.PopupViewHistoryQueryRepository;
import com.spoons.popparazzi.util.SearchKeywordNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopupSearchServiceImpl implements PopupSearchService {

    private final PopupSearchQueryRepository popupSearchQueryRepository;
    private final FileThumbQueryRepository fileThumbQueryRepository;
    private final PopupViewHistoryQueryRepository popupViewHistoryQueryRepository;
    private final LikeRepository likeRepository;

    @Override
    public PopupSearchMatchResult findBestMatch(PopupSearchMatchCommand command) {
        String normalizedKeyword = SearchKeywordNormalizer.normalize(command.keyword());

        return popupSearchQueryRepository.findBestMatch(normalizedKeyword)
                .map(this::toResult)
                .orElse(null);
    }

    private PopupSearchMatchResult toResult(PopupSearchMatchQuery query) {
        String thumbnailUrl = findPopupThumbnail(query.popupCode());

        long likeCount = likeRepository.countByTargetCodeAndType(
                query.popupCode(),
                LikeType.P
        );

        long viewCount = popupViewHistoryQueryRepository.countViews(query.popupCode());

        return new PopupSearchMatchResult(
                query.popupCode(),
                thumbnailUrl,
                query.title(),
                query.address(),
                query.startDt(),
                query.endDt(),
                likeCount,
                viewCount
        );
    }

    private String findPopupThumbnail(String popupCode) {
        return fileThumbQueryRepository.findFirstThumb(FileType.P, popupCode)
                .map(FileThumbQuery::url)
                .orElse(null);
    }
}