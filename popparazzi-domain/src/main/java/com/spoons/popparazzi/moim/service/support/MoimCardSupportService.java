package com.spoons.popparazzi.moim.service.support;

import com.spoons.popparazzi.category.dto.query.MoimCategoryRow;
import com.spoons.popparazzi.category.repository.CategoryQueryRepository;
import com.spoons.popparazzi.like.enums.LikeType;
import com.spoons.popparazzi.like.repository.LikeQueryRepository;
import com.spoons.popparazzi.moim.dto.query.MoimParticipantProfileQuery;
import com.spoons.popparazzi.moim.dto.query.main.MoimParticipantsCountQuery;
import com.spoons.popparazzi.moim.repository.MoimQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MoimCardSupportService {

    private final CategoryQueryRepository categoryQueryRepository;
    private final MoimQueryRepository moimQueryRepository;
    private final LikeQueryRepository likeQueryRepository;

    // moimCode → 카테고리 이름 최대 3개
    public Map<String, List<String>> getMoimCategories(List<String> moimCodes) {
        if (moimCodes == null || moimCodes.isEmpty()) return Map.of();

        List<MoimCategoryRow> rows = categoryQueryRepository.findMoimCategories(moimCodes);

        return rows.stream()
                .collect(Collectors.groupingBy(
                        MoimCategoryRow::parentCode,
                        LinkedHashMap::new,
                        Collectors.mapping(
                                MoimCategoryRow::categoryName,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> list.stream().limit(3).toList()
                                )
                        )
                ));
    }

    // moimCode → 현재 참여 인원 수
    public Map<String, Integer> getParticipantCounts(List<String> moimCodes) {
        if (moimCodes == null || moimCodes.isEmpty()) return Map.of();

        List<MoimParticipantsCountQuery> counts =
                moimQueryRepository.countApprovedParticipants(moimCodes);

        return counts.stream()
                .collect(Collectors.toMap(
                        MoimParticipantsCountQuery::moimCode,
                        count -> Math.toIntExact(count.participantsCount())
                ));
    }

    // 내가 좋아요한 모임 코드 Set
    public Set<String> getLikedMoimCodes(String memberCode, List<String> moimCodes) {
        if (moimCodes == null || moimCodes.isEmpty()) return Set.of();

        return new HashSet<>(
                likeQueryRepository.findLikedTargetCodes(memberCode, LikeType.M, moimCodes)
        );
    }

    // moimCode → 참여자 프로필 URL 최대 2개
    public Map<String, List<String>> getParticipantProfileUrls(List<String> moimCodes) {
        if (moimCodes == null || moimCodes.isEmpty()) return Map.of();

        List<MoimParticipantProfileQuery> rows =
                moimQueryRepository.findParticipantProfiles(moimCodes);

        return rows.stream()
                .filter(row -> row.profileUrl() != null && !row.profileUrl().isBlank())
                .collect(Collectors.groupingBy(
                        MoimParticipantProfileQuery::moimCode,
                        LinkedHashMap::new,
                        Collectors.mapping(
                                MoimParticipantProfileQuery::profileUrl,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> list.stream().limit(2).toList()
                                )
                        )
                ));
    }
}
