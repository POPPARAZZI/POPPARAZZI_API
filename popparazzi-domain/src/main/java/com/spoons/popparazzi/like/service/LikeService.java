package com.spoons.popparazzi.like.service;

import com.spoons.popparazzi.like.dto.LikeToggleResult;
import com.spoons.popparazzi.like.entity.LikeMapping;
import com.spoons.popparazzi.like.entity.LikeType;
import com.spoons.popparazzi.like.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    @Transactional
    public LikeToggleResult toggle(
            String memberCode,
            LikeType type,
            String targetCode
    ) {

        var existing = likeRepository
                .findByMemberCodeAndTargetCodeAndType(memberCode, targetCode, type);

        boolean liked;

        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            liked = false;
        } else {
            likeRepository.save(new LikeMapping(memberCode, targetCode, type));
            liked = true;
        }

        long count = likeRepository.countByTargetCodeAndType(targetCode, type);

        return new LikeToggleResult(
                targetCode,
                type,
                liked,
                count
        );
    }
}
