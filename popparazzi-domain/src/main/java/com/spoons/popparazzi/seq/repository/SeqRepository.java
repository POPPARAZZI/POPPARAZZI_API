package com.spoons.popparazzi.seq.repository;

import com.spoons.popparazzi.seq.entity.Seq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeqRepository extends JpaRepository<Seq, String> {

    @Query(value = "SELECT pop_getuniqucode(:tsmName, :prefix)", nativeQuery = true)
    String getUniqueCode(@Param("tsmName") String tsmName, @Param("prefix") String prefix);
}
