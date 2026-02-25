package com.spoons.popparazzi.moim.repository.newest;

import com.spoons.popparazzi.moim.dto.query.newest.NewestMoimItemQuery;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MoimQueryRepository {
    List<NewestMoimItemQuery> findNewestForMain(Pageable pageable);
}
