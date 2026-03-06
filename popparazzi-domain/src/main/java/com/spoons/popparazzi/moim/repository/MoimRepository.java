package com.spoons.popparazzi.moim.repository;

import com.spoons.popparazzi.common.YesNo;
import com.spoons.popparazzi.moim.entity.Moim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MoimRepository extends JpaRepository<Moim, String> {

    Optional<Moim> findByMoimCodeAndDeleteYn(String moimCode, YesNo deleteYn);
}
