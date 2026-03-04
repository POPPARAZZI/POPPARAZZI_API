package com.spoons.popparazzi.moim.repository;

import com.spoons.popparazzi.moim.entity.MoimMemberMapping;
import com.spoons.popparazzi.moim.entity.MoimMemberMappingId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoimMemberMappingRepository extends JpaRepository<MoimMemberMapping, MoimMemberMappingId> {
}