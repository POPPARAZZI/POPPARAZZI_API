package com.spoons.popparazzi.moim;

import com.spoons.popparazzi.like.service.LikeService;
import com.spoons.popparazzi.moim.dto.request.CreateMoimRequest;
import com.spoons.popparazzi.moim.dto.response.CreateMoimResponse;
import com.spoons.popparazzi.moim.dto.response.HotMoimCardResponse;
import com.spoons.popparazzi.moim.dto.response.MoimMainResponse;
import com.spoons.popparazzi.moim.dto.response.MoimRecommendCardResponse;
import com.spoons.popparazzi.moim.service.MoimCommandService;
import com.spoons.popparazzi.moim.service.MoimService;
import com.spoons.popparazzi.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/moim")
public class MoimController {

    private final MoimService moimService;
    private final MoimCommandService moimCommandService;
    private final LikeService likeService;

    // 모임 생성
    @PostMapping
    public ApiResponse<CreateMoimResponse> create(
            @RequestHeader("X-MEMBER-CODE") String memberCode,
            @RequestBody @Valid CreateMoimRequest request
    ) {
        log.info("CREATE MOIM CALLED: memberCode={}, popupCode={}", memberCode, request.getPopupCode());
        String moimCode = moimCommandService.create(request.toCommand(), memberCode);
        return ApiResponse.success(new CreateMoimResponse(moimCode));
    }

    // 신규 오픈 모임
    @GetMapping("/main/new")
    public ApiResponse<List<MoimMainResponse>> getNewestMoimsForMain(
            @RequestParam(defaultValue = "3") int limit,
            @RequestHeader(value = "X-MEMBER-CODE", required = false) String memberCode
    ) {
        var result = moimService.getNewestMoimsForMain(limit, memberCode);

        var response = result.stream()
                .map(it -> new MoimMainResponse(
                        it.moimCode(),
                        it.title(),
                        it.date(),
                        it.maxParticipants(),
                        it.thumbnailUrl(),
                        it.liked(),
                        it.categories()
                ))
                .toList();

        return ApiResponse.success(response);
    }

    // 핫한 모임
    @GetMapping("/main/hot")
    public ApiResponse<List<HotMoimCardResponse>> getHotMoimsForMain(
            @RequestParam(defaultValue = "10") int limit
    ) {
        var result = moimService.getHotMoimCardsForMain(limit);

        var response = result.stream()
                .map(it -> new HotMoimCardResponse(
                        it.moimCode(),
                        it.title(),
                        it.date(),
                        it.currentParticipants(),
                        it.maxParticipants(),
                        it.thumbnailUrl(),
                        it.likeCountToday()
                ))
                .toList();

        return ApiResponse.success(response);
    }

    // 즐겨찾기 기반 모임 추천
    @GetMapping("/main/recommend")
    public ApiResponse<List<MoimRecommendCardResponse>> recommendForMember(
            @RequestHeader(value = "X-MEMBER-CODE") String memberCode
    ) {
        var result = moimService.recommendForMember(memberCode);

        var response = result.stream()
                .map(it -> new MoimRecommendCardResponse(
                        it.moimCode(),
                        it.title(),
                        it.date(),
                        it.currentParticipants(),
                        it.maxParticipants(),
                        it.thumbnailUrl(),
                        it.liked()
                ))
                .toList();

        return ApiResponse.success(response);
    }
}
