package com.spoons.popparazzi.moim.service.support;

import com.spoons.popparazzi.category.dto.query.MoimCategoryRow;
import com.spoons.popparazzi.category.repository.CategoryQueryRepository;
import com.spoons.popparazzi.like.repository.LikeQueryRepository;
import com.spoons.popparazzi.moim.dto.query.recommend.MoimParticipantsCountQuery;
import com.spoons.popparazzi.moim.repository.recommend.MoimRecommendQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MoimCardSupportServiceImpl implements MoimCardSupportService {

    private final CategoryQueryRepository categoryQueryRepository;
    private final MoimRecommendQueryRepository recommendQueryRepository;
    private final LikeQueryRepository likeQueryRepository;

    // 1️⃣ 카테고리 3개
    @Override
    public Map<String, List<String>> getMoimCategories(List<String> moimCodes) {

        if (moimCodes == null || moimCodes.isEmpty()) {
            return Map.of();
        }

        List<MoimCategoryRow> rows =
                categoryQueryRepository.findMoimCategories(moimCodes);

        return rows.stream()
                .collect(Collectors.groupingBy(
                        MoimCategoryRow::parentCode,
                        Collectors.mapping(
                                MoimCategoryRow::categoryName,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> list.stream().limit(3).toList()
                                )
                        )
                ));
    }

    // 2️⃣ 참여 인원 수
    @Override
    public Map<String, Integer> getParticipantCounts(List<String> moimCodes) {

        if (moimCodes == null || moimCodes.isEmpty()) {
            return Map.of();
        }

        List<MoimParticipantsCountQuery> counts =
                recommendQueryRepository.countApprovedParticipants(moimCodes);

        return counts.stream()
                .collect(Collectors.toMap(
                        MoimParticipantsCountQuery::moimCode,
                        it -> Math.toIntExact(it.participantsCount())
                ));
    }

    // 3️⃣ liked 여부
    @Override
    public Set<String> getLikedMoimCodes(String memberCode, List<String> moimCodes) {

        if (memberCode == null || moimCodes == null || moimCodes.isEmpty()) {
            return Set.of();
        }

        return new HashSet<>(
                likeQueryRepository.findLikedMoimCodes(memberCode, moimCodes)
        );
    }
}