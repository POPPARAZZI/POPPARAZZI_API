package com.spoons.popparazzi.like;

import com.spoons.popparazzi.jwt.security.CustomUserDetails;
import com.spoons.popparazzi.like.dto.request.ToggleLikeRequest;
import com.spoons.popparazzi.like.dto.response.ToggleLikeResponse;
import com.spoons.popparazzi.like.service.LikeService;
import com.spoons.popparazzi.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/likes")
public class LikeController {

    private final LikeService likeService;

    // 좋아요 토글 (로그인 필수)
    @PostMapping("/toggle")
    public ApiResponse<ToggleLikeResponse> toggle(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ToggleLikeRequest request
    ) {
        String memberCode = userDetails.getMemberCode();

        log.info("[LikeToggle] memberCode={}, type={}, targetCode={}",
                memberCode, request.type(), request.targetCode());

        var result = likeService.toggle(
                memberCode,
                request.type(),
                request.targetCode()
        );

        var response = new ToggleLikeResponse(
                result.targetCode(),
                result.type(),
                result.liked(),
                result.likeCount()
        );

        return ApiResponse.success(response);
    }
}