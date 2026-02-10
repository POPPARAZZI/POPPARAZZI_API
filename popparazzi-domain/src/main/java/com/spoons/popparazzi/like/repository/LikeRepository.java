package com.spoons.popparazzi.like.repository;

import com.spoons.popparazzi.like.entity.LikeMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<LikeMapping, Long> {
}
