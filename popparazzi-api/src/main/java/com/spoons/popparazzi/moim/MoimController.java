package com.spoons.popparazzi.moim;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spoons.popparazzi.error.exception.BusinessException;
import com.spoons.popparazzi.moim.dto.command.ApplyMoimCommand;
import com.spoons.popparazzi.moim.dto.command.MoimSearchCommand;
import com.spoons.popparazzi.moim.dto.request.ApplyMoimRequest;
import com.spoons.popparazzi.moim.dto.request.CreateMoimRequest;
import com.spoons.popparazzi.moim.dto.request.MoimSearchRequest;
import com.spoons.popparazzi.moim.dto.request.UpdateMoimRequest;
import com.spoons.popparazzi.moim.dto.response.*;
import com.spoons.popparazzi.moim.dto.result.*;
import com.spoons.popparazzi.moim.error.MoimErrorCode;
import com.spoons.popparazzi.moim.service.MoimApplyService;
import com.spoons.popparazzi.moim.service.MoimCommandService;
import com.spoons.popparazzi.moim.service.MoimSearchService;
import com.spoons.popparazzi.moim.service.MoimService;
import com.spoons.popparazzi.popup.dto.response.PopupSearchMatchResponse;
import com.spoons.popparazzi.popup.dto.result.PopupSearchMatchResult;
import com.spoons.popparazzi.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
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
    private final MoimApplyService moimApplyService;
    private final MoimSearchService moimSearchService;
    private final Validator validator;

    /*----------------------------------- 모임 메인 -----------------------------------*/
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

    /*----------------------------------- 모임 조회 -----------------------------------*/
    // 1. 모임 상세 조회
    @GetMapping("/{moimCode}")
    public ApiResponse<MoimDetailResponse> getMoimDetail(
            @PathVariable String moimCode,
            @RequestHeader("X-MEMBER-CODE") String memberCode
    ) {
        MoimDetailResult result = moimService.getMoimDetail(moimCode, memberCode);
        MoimDetailResponse response = toResponse(result);
        return ApiResponse.success(response);
    }

    private MoimDetailResponse toResponse(MoimDetailResult result) {

        List<MoimDetailImageResponse> images =
                result.images()
                        .stream()
                        .map(it -> new MoimDetailImageResponse(
                                it.fileSeq(),
                                it.url()
                        ))
                        .toList();

        return new MoimDetailResponse(
                result.moimCode(),
                result.title(),
                result.content(),
                result.moimDate(),
                result.maxParticipants(),
                result.leaderMemberCode(),
                result.leaderProfileUrl(),
                images,
                result.likeCount(),
                result.liked(),
                result.participantCount(),
                result.extraParticipantCount(),
                result.owner()
        );
    }

    // 2. 모임 신청 화면 조회
    @GetMapping("/{moimCode}/apply")
    public ApiResponse<MoimApplyInfoResponse> getMoimApplyInfo(
            @PathVariable String moimCode,
            @RequestHeader("X-MEMBER-CODE") String memberCode
    ) {
        MoimApplyInfoResult result = moimApplyService.getApplyInfo(moimCode, memberCode);

        MoimApplyInfoResponse response = new MoimApplyInfoResponse(
                result.leaderProfileImageUrl(),
                result.leaderNickname(),
                result.question()
        );

        return ApiResponse.success(response);
    }

    // 3. 모임 참여 멤버 목록 조회
    @GetMapping("/{moimCode}/participants")
    public ApiResponse<MoimParticipantsResponse> getParticipants(
            @PathVariable String moimCode,
            @RequestHeader("X-MEMBER-CODE") String memberCode
    ) {
        MoimParticipantsResult result = moimApplyService.getParticipants(moimCode, memberCode);

        MoimParticipantsResponse response = MoimParticipantsResponse.from(result);

        return ApiResponse.success(response);
    }

    /*----------------------------------- 모임 검색 -----------------------------------*/
    @PostMapping("/search")
    public ApiResponse<MoimSearchResponse> searchMoims(
            @RequestHeader("X-MEMBER-CODE") String memberCode,
            @RequestBody MoimSearchRequest request
    ) {
        MoimSearchCommand command = new MoimSearchCommand(
                memberCode,
                request.getKeyword(),
                request.getPaginationInfo()
        );

        MoimSearchResult result = moimSearchService.searchMoims(command);

        MoimSearchResponse response = new MoimSearchResponse(
                result.keyword(),
                toPopupResponse(result.matchedPopup()),
                toItemResponses(result.moims()),
                result.currentPage(),
                result.recordCountPerPage(),
                result.totalRecord(),
                result.totalPage()
        );

        return ApiResponse.success(response);
    }

    private PopupSearchMatchResponse toPopupResponse(PopupSearchMatchResult popup) {
        if (popup == null) {
            return null;
        }

        return new PopupSearchMatchResponse(
                popup.popupCode(),
                popup.thumbnailUrl(),
                popup.title(),
                popup.address(),
                popup.startDt(),
                popup.endDt(),
                popup.likeCount(),
                popup.viewCount()
        );
    }

    private List<MoimSearchItemResponse> toItemResponses(List<MoimSearchCardResult> cards) {
        return cards.stream()
                .map(card -> new MoimSearchItemResponse(
                        card.moimCode(),
                        card.thumbnailUrl(),
                        card.liked(),
                        card.categories(),
                        card.title(),
                        card.address(),
                        card.participantProfileUrls(),
                        card.leaderNickname(),
                        card.currentCount(),
                        card.maxCount(),
                        card.closingSoon()
                ))
                .toList();
    }
    /*----------------------------------- 모임 CRUD -----------------------------------*/
    // 1. 모임 생성
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CreateMoimResponse> createMoim(
            @RequestHeader("X-MEMBER-CODE") String memberCode,
            @RequestPart("request") String requestJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        CreateMoimRequest request = parseRequest(requestJson, CreateMoimRequest.class);
        validate(request);

        String moimCode = moimCommandService.create(request.toCommand(), files, memberCode);
        return ApiResponse.success(new CreateMoimResponse(moimCode));
    }

    // 2. 모임 수정
    @PatchMapping(value = "/{moimCode}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UpdateMoimResponse> updateMoim(
            @PathVariable String moimCode,
            @RequestHeader("X-MEMBER-CODE") String memberCode,
            @RequestPart("request") String requestJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        UpdateMoimRequest request = parseRequest(requestJson, UpdateMoimRequest.class);
        validate(request);

        String updatedMoimCode = moimCommandService.update(
                request.toCommand(moimCode),
                files,
                memberCode
        );

        return ApiResponse.success(new UpdateMoimResponse(updatedMoimCode));
    }

    // 3. 모임 삭제
    @DeleteMapping("/{moimCode}")
    public ApiResponse<Void> deleteMoim(
            @PathVariable String moimCode,
            @RequestHeader("X-MEMBER-CODE") String requesterMemberCode
    ) {
        moimCommandService.delete(moimCode, requesterMemberCode);
        return ApiResponse.success(null);
    }

    // 4. 모임 신청
    @PostMapping("/{moimCode}/apply")
    public ApiResponse<Void> applyMoim(
            @PathVariable String moimCode,
            @RequestBody @Valid ApplyMoimRequest request,
            @RequestHeader("X-MEMBER-CODE") String memberCode
    ) {

        ApplyMoimCommand command = new ApplyMoimCommand(request.answer());
        moimApplyService.apply(moimCode, memberCode, command);
        return ApiResponse.success();
    }

    /*----------------------------------- 공통 -----------------------------------*/
    // 1. JSON 파싱
    private <T> T parseRequest(String requestJson, Class<T> clazz) {
        try {
            return objectMapper.readValue(requestJson, clazz);
        } catch (JsonProcessingException e) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }
    }

    // 2. 어노테이션 검증
    private <T> void validate(T request) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);

        if (!violations.isEmpty()) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }
    }
}