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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MoimCardSupportServiceImpl implements MoimCardSupportService {

    private final CategoryQueryRepository categoryQueryRepository;
    private final MoimQueryRepository moimQueryRepository;
    private final LikeQueryRepository likeQueryRepository;

    @Override
    public Map<String, List<String>> getMoimCategories(List<String> moimCodes) {
        if (moimCodes == null || moimCodes.isEmpty()) {
            return Map.of();
        }

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

    @Override
    public Map<String, Integer> getParticipantCounts(List<String> moimCodes) {
        if (moimCodes == null || moimCodes.isEmpty()) {
            return Map.of();
        }

        List<MoimParticipantsCountQuery> counts =
                moimQueryRepository.countApprovedParticipants(moimCodes);

        return counts.stream()
                .collect(Collectors.toMap(
                        MoimParticipantsCountQuery::moimCode,
                        count -> Math.toIntExact(count.participantsCount())
                ));
    }

    @Override
    public Set<String> getLikedMoimCodes(String memberCode, List<String> moimCodes) {
        if (memberCode == null || memberCode.isBlank() || moimCodes == null || moimCodes.isEmpty()) {
            return Set.of();
        }

        return new HashSet<>(
                likeQueryRepository.findLikedTargetCodes(memberCode, LikeType.M, moimCodes)
        );
    }

    @Override
    public Map<String, List<String>> getParticipantProfileUrls(List<String> moimCodes) {
        if (moimCodes == null || moimCodes.isEmpty()) {
            return Map.of();
        }

        List<MoimParticipantProfileQuery> rows =
                moimQueryRepository.findParticipantProfiles(moimCodes);

        return rows.stream()
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