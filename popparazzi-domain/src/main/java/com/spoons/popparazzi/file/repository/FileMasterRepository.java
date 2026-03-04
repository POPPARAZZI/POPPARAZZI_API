package com.spoons.popparazzi.file.repository;

import com.spoons.popparazzi.file.entity.FileMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface FileMasterRepository extends JpaRepository<FileMaster, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update FileMaster f
           set f.parentCode = :parentCode
         where f.fmSeq in :fileSeqs
           and f.parentCode = :tempCode
    """)
    int attachToParentFromTemp(
            @Param("fileSeqs") Collection<Long> fileSeqs,
            @Param("parentCode") String parentCode,
            @Param("tempCode") String tempCode
    );
}