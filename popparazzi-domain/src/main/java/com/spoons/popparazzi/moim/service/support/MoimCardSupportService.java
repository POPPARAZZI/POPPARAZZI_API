package com.spoons.popparazzi.moim.service.support;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MoimCardSupportService {

    // moimCode → 카테고리 이름 최대 3개
    Map<String, List<String>> getMoimCategories(List<String> moimCodes);

    // moimCode → 현재 참여 인원 수, 목록용 bulk
    Map<String, Integer> getParticipantCounts(List<String> moimCodes);

    // liked moimCode Set
    Set<String> getLikedMoimCodes(String memberCode, List<String> moimCodes);
}