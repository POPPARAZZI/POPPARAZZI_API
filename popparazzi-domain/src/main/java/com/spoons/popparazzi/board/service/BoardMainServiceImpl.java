package com.spoons.popparazzi.board.service;

import com.spoons.popparazzi.board.dto.query.HotMoimBoardItemQuery;
import com.spoons.popparazzi.board.dto.result.HotBoardCardResult;
import com.spoons.popparazzi.board.repository.BoardQueryRepository;
import com.spoons.popparazzi.config.ImageProperties;
import com.spoons.popparazzi.file.dto.query.FileThumbQuery;
import com.spoons.popparazzi.file.enums.FileType;
import com.spoons.popparazzi.file.repository.FileThumbQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardMainServiceImpl implements BoardMainService {

    private static final int RESULT_SIZE = 4;

    private final BoardQueryRepository boardQueryRepository;
    private final FileThumbQueryRepository fileThumbQueryRepository;
    private final ImageProperties imageProperties;

    @Override
    public List<HotBoardCardResult> getHotMoimBoardsForMain() {

        // 1) 최근 7일 계산
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusDays(7);

        // 2) 좋아요 기준 Top4 조회 (좋아요 0개 포함 + 동률 최신순)
        List<HotMoimBoardItemQuery> items = boardQueryRepository.findHotMoimBoards(from, to);
        if (items.isEmpty()) {
            return List.of();
        }

        // 3) boardCode 리스트 추출
        List<String> boardCodes = items.stream()
                .map(HotMoimBoardItemQuery::boardCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 4) 썸네일 조회 (없을 수도 있음)
        Map<String, String> thumbMap = Collections.emptyMap();

        if (!boardCodes.isEmpty()) {
            List<FileThumbQuery> thumbs = fileThumbQueryRepository.findFirstThumbs(FileType.R, boardCodes);

            thumbMap = thumbs.stream()
                    .filter(t -> t.parentCode() != null && t.url() != null)
                    .collect(Collectors.toMap(
                            FileThumbQuery::parentCode,
                            FileThumbQuery::url,
                            (a, b) -> a
                    ));
        }

        // 5) 기본 이미지 URL (설정에서)
        String defaultThumb = imageProperties.getDefaultBoardThumbnail();

        // 6) 결과 조립: 썸네일 없으면 기본 이미지로 대체
        List<HotBoardCardResult> results = new ArrayList<>(Math.min(RESULT_SIZE, items.size()));

        for (HotMoimBoardItemQuery item : items) {
            String thumbnailUrl = thumbMap.getOrDefault(item.boardCode(), defaultThumb);

            results.add(new HotBoardCardResult(
                    item.boardCode(),
                    item.title(),
                    thumbnailUrl,
                    item.likeCount()
            ));

            if (results.size() == RESULT_SIZE) {
                break;
            }
        }

        return results;
    }
}