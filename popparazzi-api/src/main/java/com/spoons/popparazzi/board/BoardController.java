package com.spoons.popparazzi.board;

import com.spoons.popparazzi.board.dto.response.HotBoardCardResponse;
import com.spoons.popparazzi.board.service.BoardMainService;
import com.spoons.popparazzi.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardMainService boardMainService;

    // 모임 메인 - 인기 모임 후기(최근 7일 좋아요 TOP 4)
    @GetMapping("/main/hot-moim-reviews")
    public ApiResponse<List<HotBoardCardResponse>> getHotMoimReviewsForMain() {

        var result = boardMainService.getHotMoimBoardsForMain();

        var response = result.stream()
                .map(it -> new HotBoardCardResponse(
                        it.boardCode(),
                        it.title(),
                        it.thumbnailUrl(),
                        it.likeCount()
                ))
                .toList();

        return ApiResponse.success(response);
    }
}