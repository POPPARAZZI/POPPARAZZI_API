package com.spoons.popparazzi.like.repository;

import com.spoons.popparazzi.like.entity.LikeMapping;
import com.spoons.popparazzi.like.entity.LikeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeMapping, Long> {

    Optional<LikeMapping> findByMemberCodeAndTargetCodeAndType(
            String memberCode,
            String targetCode,
            LikeType type
    );

    long countByTargetCodeAndType(String targetCode, LikeType type);

    void deleteByMemberCodeAndTargetCodeAndType(
            String memberCode,
            String targetCode,
            LikeType type
    );
}
