package com.spoons.popparazzi.moim.service;

import com.spoons.popparazzi.error.exception.BusinessException;
import com.spoons.popparazzi.file.dto.query.MoimThumbTarget;
import com.spoons.popparazzi.file.service.FileThumbnailService;
import com.spoons.popparazzi.moim.dto.command.MoimSearchCommand;
import com.spoons.popparazzi.moim.dto.query.MoimSearchItemQuery;
import com.spoons.popparazzi.moim.dto.result.MoimSearchCardResult;
import com.spoons.popparazzi.moim.dto.result.MoimSearchResult;
import com.spoons.popparazzi.moim.error.MoimErrorCode;
import com.spoons.popparazzi.moim.repository.MoimSearchQueryRepository;
import com.spoons.popparazzi.moim.service.support.MoimCardSupportService;
import com.spoons.popparazzi.popup.dto.command.PopupSearchMatchCommand;
import com.spoons.popparazzi.popup.dto.result.PopupSearchMatchResult;
import com.spoons.popparazzi.popup.service.PopupSearchService;
import com.spoons.popparazzi.util.PaginationInfo;
import com.spoons.popparazzi.util.SearchKeywordNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MoimSearchServiceImpl implements MoimSearchService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;

    private final MoimSearchQueryRepository moimSearchQueryRepository;
    private final MoimCardSupportService moimCardSupportService;
    private final FileThumbnailService fileThumbnailService;
    private final PopupSearchService popupSearchService;

    @Override
    public MoimSearchResult searchMoims(MoimSearchCommand command) {
        validateKeyword(command.keyword());

        String normalizedKeyword = SearchKeywordNormalizer.normalize(command.keyword());
        PaginationInfo paginationInfo = getOrCreatePagination(command.paginationInfo());

        PopupSearchMatchResult matchedPopup = popupSearchService.findBestMatch(
                new PopupSearchMatchCommand(command.keyword())
        );

        long totalCount = moimSearchQueryRepository.countSearchMoims(normalizedKeyword);
        paginationInfo.setTotalRecord(Math.toIntExact(totalCount));
        paginationInfo.pageInit();

        if (totalCount == 0) {
            return new MoimSearchResult(
                    command.keyword(),
                    matchedPopup,
                    List.of(),
                    paginationInfo.getCurrentPage(),
                    paginationInfo.getRecordCountPerPage(),
                    paginationInfo.getTotalRecord(),
                    paginationInfo.getTotalPage()
            );
        }

        List<MoimSearchItemQuery> items =
                moimSearchQueryRepository.searchMoims(normalizedKeyword, paginationInfo);

        if (items.isEmpty()) {
            return new MoimSearchResult(
                    command.keyword(),
                    matchedPopup,
                    List.of(),
                    paginationInfo.getCurrentPage(),
                    paginationInfo.getRecordCountPerPage(),
                    paginationInfo.getTotalRecord(),
                    paginationInfo.getTotalPage()
            );
        }

        List<String> moimCodes = items.stream()
                .map(MoimSearchItemQuery::moimCode)
                .toList();

        Set<String> likedSet =
                moimCardSupportService.getLikedMoimCodes(command.memberCode(), moimCodes);

        Map<String, List<String>> categoryMap =
                moimCardSupportService.getMoimCategories(moimCodes);

        Map<String, Integer> participantCountMap =
                moimCardSupportService.getParticipantCounts(moimCodes);

        Map<String, List<String>> participantProfileMap =
                moimCardSupportService.getParticipantProfileUrls(moimCodes);

        Map<String, String> thumbMap =
                fileThumbnailService.getMoimThumbsWithPopupFallback(
                        toThumbTargets(items, MoimSearchItemQuery::moimCode, MoimSearchItemQuery::popupCode)
                );

        List<MoimSearchCardResult> cards = items.stream()
                .map(item -> {
                    int currentCount = participantCountMap.getOrDefault(item.moimCode(), 1);
                    int maxCount = item.maxCount() == null ? 0 : item.maxCount();

                    return new MoimSearchCardResult(
                            item.moimCode(),
                            thumbMap.get(item.moimCode()),
                            likedSet.contains(item.moimCode()),
                            categoryMap.getOrDefault(item.moimCode(), List.of()),
                            item.title(),
                            item.address(),
                            participantProfileMap.getOrDefault(item.moimCode(), List.of()),
                            item.leaderNickname(),
                            currentCount,
                            maxCount,
                            isClosingSoon(currentCount, maxCount)
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

    private void validateKeyword(String keyword) {
        if (!SearchKeywordNormalizer.isValidLength(keyword)) {
            throw new BusinessException(MoimErrorCode.INVALID_SEARCH_KEYWORD);
        }
    }

    private PaginationInfo getOrCreatePagination(PaginationInfo paginationInfo) {
        if (paginationInfo != null) {
            return paginationInfo;
        }

        PaginationInfo defaultPagination = new PaginationInfo();
        defaultPagination.setCurrentPage(DEFAULT_PAGE);
        defaultPagination.setRecordCountPerPage(DEFAULT_SIZE);
        return defaultPagination;
    }

    private boolean isClosingSoon(int currentCount, int maxCount) {
        if (maxCount <= 0) {
            return false;
        }

        int remainCount = maxCount - currentCount;

        if (maxCount <= 5) {
            return remainCount == 1;
        }

        return remainCount <= 2;
    }

    private <T> List<MoimThumbTarget> toThumbTargets(
            List<T> items,
            Function<T, String> moimCodeGetter,
            Function<T, String> popupCodeGetter
    ) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        return items.stream()
                .map(item -> new MoimThumbTarget(
                        moimCodeGetter.apply(item),
                        popupCodeGetter.apply(item)
                ))
                .toList();
    }
}