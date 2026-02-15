package com.spoons.popparazzi.moim.repository;

import com.spoons.popparazzi.moim.entity.Moim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoimRepository extends JpaRepository<Moim, String> {
}
