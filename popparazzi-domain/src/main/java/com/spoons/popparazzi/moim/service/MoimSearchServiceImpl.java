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

        // 상단 팝업은 존재하지만, 모임은 존재하지 않을 수 있음
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

        // bulk 조회
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
                    int participantCount = participantCountMap.getOrDefault(item.moimCode(), 1);
                    int maxParticipantCount = item.maxCount() == null ? 0 : item.maxCount();

                    return new MoimSearchCardResult(
                            item.moimCode(),
                            thumbMap.get(item.moimCode()),
                            likedSet.contains(item.moimCode()),
                            categoryMap.getOrDefault(item.moimCode(), List.of()),
                            item.title(),
                            item.address(),
                            participantProfileMap.getOrDefault(item.moimCode(), List.of()),
                            item.leaderNickname(),
                            participantCount,
                            maxParticipantCount,
                            isClosingSoon(participantCount, maxParticipantCount)
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

    // 검색어 검증
    private void validateKeyword(String keyword) {
        if (!SearchKeywordNormalizer.isValidLength(keyword)) {
            throw new BusinessException(MoimErrorCode.INVALID_SEARCH_KEYWORD);
        }
    }

    // 페이징 정보
    private PaginationInfo getOrCreatePagination(PaginationInfo paginationInfo) {
        if (paginationInfo != null) {
            return paginationInfo;
        }

        PaginationInfo defaultPagination = new PaginationInfo();
        defaultPagination.setCurrentPage(DEFAULT_PAGE);
        defaultPagination.setRecordCountPerPage(DEFAULT_SIZE);
        return defaultPagination;
    }

    // 마감 임박 계산 (5명 이하 : 1자리, 6명 이상 : 2자리)
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

    // 썸네일 조회용 타겟 리스트 변환
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