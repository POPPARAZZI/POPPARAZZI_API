package com.spoons.popparazzi.moim;

import com.spoons.popparazzi.like.service.LikeService;
import com.spoons.popparazzi.moim.dto.request.CreateMoimRequest;
import com.spoons.popparazzi.moim.dto.response.HotMoimCardResponse;
import com.spoons.popparazzi.moim.dto.response.MoimMainResponse;
import com.spoons.popparazzi.moim.service.MoimService;
import com.spoons.popparazzi.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/moim")
public class MoimController {

    private final MoimService moimService;
    private final LikeService likeService;

    @PostMapping
    public ResponseEntity<Long> create(
            @RequestBody @Valid CreateMoimRequest request
    ) {
        Long meetingId = moimService.create(request.toCommand());
        return ResponseEntity.ok(meetingId);
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
                        it.likeCount24h()
                ))
                .toList();

        return ApiResponse.success(response);
    }
}
