package com.spoons.popparazzi.moim.repository;

import com.spoons.popparazzi.moim.dto.query.NewestMoimItemQuery;
import com.spoons.popparazzi.moim.entity.Moim;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MoimRepository extends JpaRepository<Moim, String> {

    @Query("""
        select new com.spoons.popparazzi.moim.dto.query.NewestMoimItemQuery(
            m.mmCode, m.mmTitle, m.mmDate, m.mmMaxParticipants
        )
        from Moim m
        where m.mmDeleteYn = 'N'
        order by m.mmRegDt desc 
""")
    List<NewestMoimItemQuery> findNewestForMain(Pageable pageable);

}
