package com.spoons.popparazzi.moim.repository.newest;

import com.spoons.popparazzi.moim.dto.query.MoimDetailQuery;
import com.spoons.popparazzi.moim.dto.query.newest.NewestMoimItemQuery;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MoimQueryRepository {
    // 1. 메인 - 신규 모임 조회
    List<NewestMoimItemQuery> findNewestForMain(Pageable pageable);

    // 2. 모임 상세 조회
    MoimDetailQuery findMoimDetail(String moimCode);
}
