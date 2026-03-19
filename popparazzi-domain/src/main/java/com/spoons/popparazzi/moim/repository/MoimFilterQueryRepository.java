package com.spoons.popparazzi.moim.repository;

import com.spoons.popparazzi.moim.dto.command.MoimFilterCommand;
import com.spoons.popparazzi.moim.dto.query.MoimFilterItemQuery;
import org.springframework.data.domain.Slice;

public interface MoimFilterQueryRepository {

    Slice<MoimFilterItemQuery> searchMoimsByFilter(MoimFilterCommand command);
}