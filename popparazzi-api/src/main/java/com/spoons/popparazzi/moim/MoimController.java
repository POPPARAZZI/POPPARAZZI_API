package com.spoons.popparazzi.moim;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spoons.popparazzi.error.exception.BusinessException;
import com.spoons.popparazzi.jwt.security.CustomUserDetails;
import com.spoons.popparazzi.moim.dto.request.*;
import com.spoons.popparazzi.moim.dto.response.*;
import com.spoons.popparazzi.moim.error.MoimErrorCode;
import com.spoons.popparazzi.moim.service.MoimApplyService;
import com.spoons.popparazzi.moim.service.MoimCommandService;
import com.spoons.popparazzi.moim.service.MoimQueryService;
import com.spoons.popparazzi.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/moim")
@Tag(name = "모임", description = "모임 생성/수정/삭제/조회/검색/필터/신청 API")
public class MoimController {

    private final ObjectMapper objectMapper;
    private final MoimQueryService moimQueryService;
    private final MoimCommandService moimCommandService;
    private final MoimApplyService moimApplyService;
    private final Validator validator;

    /*----------------------------------- 모임 메인 -----------------------------------*/

    /**
     * @methodName  : getNewestMoimsForMain
     * @author      : seulgi Yang
     * @param       : limit, userDetails
     * @returnType  : ApiResponse<List<NewesCardResponse>>
     * @desc        : 메인 화면 신규 오픈 모임 카드 목록 조회
     */
    @GetMapping("/main/new")
    public ApiResponse<List<NewesCardResponse>> getNewestMoimsForMain(
            @RequestParam(defaultValue = "3") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        var response = moimQueryService.getNewestMoimsForMain(limit, userDetails.getMemberCode())
                .stream()
                .map(NewesCardResponse::from)
                .toList();

        return ApiResponse.success(response);
    }

    /**
     * @methodName  : getHotMoimsForMain
     * @author      : seulgi Yang
     * @param       : limit
     * @returnType  : ApiResponse<List<HotMoimCardResponse>>
     * @desc        : 메인 화면 핫 모임 카드 목록 조회 (오늘 좋아요 기준, 폴백: 최신순)
     */
    @GetMapping("/main/hot")
    public ApiResponse<List<HotMoimCardResponse>> getHotMoimsForMain(
            @RequestParam(defaultValue = "10") int limit
    ) {
        var response = moimQueryService.getHotMoimCardsForMain(limit)
                .stream()
                .map(HotMoimCardResponse::from)
                .toList();

        return ApiResponse.success(response);
    }

    /**
     * @methodName  : recommendForMember
     * @author      : seulgi Yang
     * @param       : userDetails
     * @returnType  : ApiResponse<List<MoimRecommendCardResponse>>
     * @desc        : 회원 선호 지역/카테고리 기반 모임 추천 (폴백: 최신순)
     */
    @GetMapping("/main/recommend")
    public ApiResponse<List<MoimRecommendCardResponse>> recommendForMember(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        var response = moimQueryService.recommendForMember(userDetails.getMemberCode())
                .stream()
                .map(MoimRecommendCardResponse::from)
                .toList();

        return ApiResponse.success(response);
    }

    /*----------------------------------- 모임 조회 -----------------------------------*/

    /**
     * @methodName  : getMoimDetail
     * @author      : seulgi Yang
     * @param       : moimCode, userDetails
     * @returnType  : ApiResponse<MoimDetailResponse>
     * @desc        : 모임 상세 조회
     */
    @GetMapping("/{moimCode}")
    public ApiResponse<MoimDetailResponse> getMoimDetail(
            @PathVariable String moimCode,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(
                MoimDetailResponse.from(
                        moimQueryService.getMoimDetail(moimCode, userDetails.getMemberCode())
                )
        );
    }

    /**
     * @methodName  : getMoimApplyInfo
     * @author      : seulgi Yang
     * @param       : moimCode, userDetails
     * @returnType  : ApiResponse<MoimApplyInfoResponse>
     * @desc        : 모임 신청 화면 조회 (방장 프로필 + 사전 질문)
     */
    @GetMapping("/{moimCode}/apply")
    public ApiResponse<MoimApplyInfoResponse> getMoimApplyInfo(
            @PathVariable String moimCode,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(
                MoimApplyInfoResponse.from(
                        moimApplyService.getApplyInfo(moimCode, userDetails.getMemberCode())
                )
        );
    }

    /**
     * @methodName  : getParticipants
     * @author      : seulgi Yang
     * @param       : moimCode, userDetails
     * @returnType  : ApiResponse<MoimParticipantsResponse>
     * @desc        : 모임 참여자 목록 조회 (방장 우선 정렬)
     */
    @GetMapping("/{moimCode}/participants")
    public ApiResponse<MoimParticipantsResponse> getParticipants(
            @PathVariable String moimCode,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(
                MoimParticipantsResponse.from(
                        moimApplyService.getParticipants(moimCode, userDetails.getMemberCode())
                )
        );
    }

    /**
     * @methodName  : getMoimsByFilter
     * @author      : seulgi Yang
     * @param       : userDetails, request
     * @returnType  : ApiResponse<MoimFilterSliceResponse>
     * @desc        : 모임 필터 조회 (NEW / HOT / FAVORITE, 폴백: NEW)
     */
    @GetMapping
    public ApiResponse<MoimFilterSliceResponse> getMoimsByFilter(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid MoimFilterRequest request
    ) {
        return ApiResponse.success(
                MoimFilterSliceResponse.from(
                        moimQueryService.getMoimsByFilter(request.toCommand(userDetails.getMemberCode()))
                )
        );
    }

    /*----------------------------------- 모임 검색 -----------------------------------*/

    /**
     * @methodName  : searchMoims
     * @author      : seulgi Yang
     * @param       : userDetails, request
     * @returnType  : ApiResponse<MoimSearchResponse>
     * @desc        : 모임 키워드 검색 (팝업 매칭 + 모임 목록 페이징)
     */
    @PostMapping("/search")
    public ApiResponse<MoimSearchResponse> searchMoims(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid MoimSearchRequest request
    ) {
        return ApiResponse.success(
                MoimSearchResponse.from(
                        moimQueryService.searchMoims(request.toCommand(userDetails.getMemberCode()))
                )
        );
    }

    /*----------------------------------- 모임 CRUD -----------------------------------*/

    /**
     * @methodName  : createMoim
     * @author      : seulgi Yang
     * @param       : userDetails, requestJson, files
     * @returnType  : ApiResponse<CreateMoimResponse>
     * @desc        : 모임 생성. 모임 저장 + 카테고리 매핑 + 파일 저장 일괄 처리
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CreateMoimResponse> createMoim(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("request") String requestJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        CreateMoimRequest request = parseRequest(requestJson, CreateMoimRequest.class);
        validate(request);

        String moimCode = moimCommandService.create(request.toCommand(), files, userDetails.getMemberCode());
        return ApiResponse.success(new CreateMoimResponse(moimCode));
    }

    /**
     * @methodName  : updateMoim
     * @author      : seulgi Yang
     * @param       : moimCode, userDetails, requestJson, files
     * @returnType  : ApiResponse<UpdateMoimResponse>
     * @desc        : 모임 수정. 카테고리 전체 교체 + 파일 유지/삭제/추가 처리
     */
    @PatchMapping(value = "/{moimCode}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UpdateMoimResponse> updateMoim(
            @PathVariable String moimCode,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("request") String requestJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        UpdateMoimRequest request = parseRequest(requestJson, UpdateMoimRequest.class);
        validate(request);

        String updatedMoimCode = moimCommandService.update(
                request.toCommand(moimCode), files, userDetails.getMemberCode()
        );

        return ApiResponse.success(new UpdateMoimResponse(updatedMoimCode));
    }

    /**
     * @methodName  : deleteMoim
     * @author      : seulgi Yang
     * @param       : moimCode, userDetails
     * @returnType  : ApiResponse<Void>
     * @desc        : 모임 삭제. 당일 삭제 불가. 첨부파일 삭제 + soft delete 처리
     */
    @DeleteMapping("/{moimCode}")
    public ApiResponse<Void> deleteMoim(
            @PathVariable String moimCode,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        moimCommandService.delete(moimCode, userDetails.getMemberCode());
        return ApiResponse.success(null);
    }

    /**
     * @methodName  : applyMoim
     * @author      : seulgi Yang
     * @param       : moimCode, request, userDetails
     * @returnType  : ApiResponse<Void>
     * @desc        : 모임 참여 신청. 방장 신청 불가, 중복 신청 불가, 정원 초과 불가
     */
    @PostMapping("/{moimCode}/apply")
    public ApiResponse<Void> applyMoim(
            @PathVariable String moimCode,
            @RequestBody @Valid ApplyMoimRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        moimApplyService.apply(moimCode, userDetails.getMemberCode(), request.toCommand());
        return ApiResponse.success();
    }

    /**
     * @methodName  : approveMoimApply
     * @author      : seulgi Yang
     * @param       : moimCode, request, userDetails
     * @returnType  : ApiResponse<Void>
     * @desc        : 모임 신청 승인. 모임장만 승인 가능, 대기 상태 신청 건만 승인 처리
     */
    @PatchMapping("/{moimCode}/apply/approve")
    public ApiResponse<Void> approveMoimApply(
            @PathVariable String moimCode,
            @RequestBody @Valid MoimApproveRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        moimApplyService.approve(
                moimCode,
                userDetails.getMemberCode(),
                request.getApplicantMemberCode()
        );
        return ApiResponse.success();
    }

    /**
     * @methodName  : rejectMoimApply
     * @author      : seulgi Yang
     * @param       : moimCode, request, userDetails
     * @returnType  : ApiResponse<Void>
     * @desc        : 모임 신청 거절. 모임장만 거절 가능, 대기 상태 신청 건만 거절 처리
     */
    @PatchMapping("/{moimCode}/apply/reject")
    public ApiResponse<Void> rejectMoimApply(
            @PathVariable String moimCode,
            @RequestBody @Valid MoimRejectRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        moimApplyService.reject(
                moimCode,
                userDetails.getMemberCode(),
                request.getApplicantMemberCode()
        );
        return ApiResponse.success();
    }


    /*----------------------------------- 내부 유틸 -----------------------------------*/

    private <T> T parseRequest(String requestJson, Class<T> clazz) {
        try {
            return objectMapper.readValue(requestJson, clazz);
        } catch (JsonProcessingException e) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }
    }

    private <T> void validate(T request) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }
    }
}
