package com.spoons.popparazzi.moim;

import com.spoons.popparazzi.moim.dto.request.CreateMoimRequest;
import com.spoons.popparazzi.moim.dto.response.HotMoimCardResponse;
import com.spoons.popparazzi.moim.dto.response.MoimMainResponse;
import com.spoons.popparazzi.moim.service.DbProbeService;
import com.spoons.popparazzi.moim.service.MoimService;
import com.spoons.popparazzi.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/moim")
public class MoimController {

    private final MoimService moimService;
    private final DbProbeService dbProbeService;

    @PostMapping
    public ResponseEntity<Long> create(
            @RequestBody @Valid CreateMoimRequest request
    ) {
        Long meetingId = moimService.create(request.toCommand());
        return ResponseEntity.ok(meetingId);
    }

    // 신규 오픈 모임
    @GetMapping("/main/new")
    public ApiResponse<List<MoimMainResponse>> getNewestMoimsForMain() {

        var result = moimService.getNewestMoimsForMain();

        var response = result.stream()
                .map(it -> new MoimMainResponse(
                        it.mmCode(),
                        it.mmTitle(),
                        it.mmDate(),
                        it.mmMaxParticipants()
                ))
                .toList();

        return ApiResponse.success(response);
    }

    @GetMapping("/main/hot")
    public ApiResponse<List<HotMoimCardResponse>> getHotMoimsForMain(
            @RequestParam(defaultValue = "10") int limit
    ) {

        dbProbeService.logDbInfo();
        var result = moimService.getHotMoimCardsForMain(limit);

        var response = result.stream()
                .map(it -> new HotMoimCardResponse(
                        it.mmCode(),
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
