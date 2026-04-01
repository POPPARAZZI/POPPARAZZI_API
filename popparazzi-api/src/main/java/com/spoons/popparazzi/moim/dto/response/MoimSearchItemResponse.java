package com.spoons.popparazzi.moim.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "모임 검색 결과 카드")
public record MoimSearchItemResponse(

        @Schema(description = "모임 코드", example = "M202603160001")
        String moimCode,

        @Schema(description = "모임 썸네일 이미지 URL", example = "https://cdn.popparazzi.com/moim/thumbnail.jpg")
        String thumbnailUrl,

        @Schema(description = "좋아요 여부", example = "true")
        boolean liked,

        @Schema(description = "카테고리 목록(최대 3개)")
        List<String> categories,

        @Schema(description = "모임명", example = "팝업 같이 갈 사람")
        String title,

        @Schema(description = "모임 지역", example = "서울 성동구")
        String address,

        @Schema(description = "참여 멤버 프로필 이미지 URL 목록(최대 2개)")
        List<String> participantProfileUrls,

        @Schema(description = "모임장 닉네임", example = "닉네임")
        String leaderNickname,

        @Schema(description = "현재 참여 인원", example = "3")
        int participantCount,

        @Schema(description = "최대 정원", example = "4")
        int maxParticipantCount,

        @Schema(description = "마감 임박 여부", example = "true")
        boolean closingSoon
) {
}