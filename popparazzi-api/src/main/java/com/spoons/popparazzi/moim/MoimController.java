package com.spoons.popparazzi.moim;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spoons.popparazzi.error.exception.BusinessException;
import com.spoons.popparazzi.like.service.LikeService;
import com.spoons.popparazzi.moim.dto.request.CreateMoimRequest;
import com.spoons.popparazzi.moim.dto.response.CreateMoimResponse;
import com.spoons.popparazzi.moim.dto.response.HotMoimCardResponse;
import com.spoons.popparazzi.moim.dto.response.MoimMainResponse;
import com.spoons.popparazzi.moim.dto.response.MoimRecommendCardResponse;
import com.spoons.popparazzi.moim.error.MoimErrorCode;
import com.spoons.popparazzi.moim.service.MoimCommandService;
import com.spoons.popparazzi.moim.service.MoimService;
import com.spoons.popparazzi.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/moim")
public class MoimController {

    private final ObjectMapper objectMapper;
    private final MoimService moimService;
    private final MoimCommandService moimCommandService;
    private final Validator validator;
    private final LikeService likeService;

    /*----------------------------------- 모임 조회 -----------------------------------*/
    // 1. 신규 오픈 모임
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

    // 2. 지금 핫한 모임
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

    // 3. 즐겨찾기 기반 모임 추천
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

    /*----------------------------------- 모임 CRUD -----------------------------------*/
    // 1. 모임 생성
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CreateMoimResponse> createMoim(
            @RequestHeader("X-MEMBER-CODE") String memberCode,
            @RequestPart("request") String requestJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        CreateMoimRequest request = parseRequest(requestJson);
        validate(request);

        String moimCode = moimCommandService.create(request.toCommand(), files, memberCode);
        return ApiResponse.success(new CreateMoimResponse(moimCode));
    }

    private CreateMoimRequest parseRequest(String requestJson) {
        try {
            return objectMapper.readValue(requestJson, CreateMoimRequest.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }
    }

    private void validate(CreateMoimRequest request) {
        Set<ConstraintViolation<CreateMoimRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }
    }

    // 2. 모임 수정


    // 3. 모임 삭제
    @DeleteMapping("/{moimCode}")
    public ApiResponse<Void> deleteMoim(
            @PathVariable String moimCode,
            @RequestHeader("X-MEMBER-CODE") String requesterMemberCode
    ) {
        moimCommandService.delete(moimCode, requesterMemberCode);
        return ApiResponse.success("모임이 삭제되었습니다.");
    }

}
