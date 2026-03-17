package com.spoons.popparazzi.moim.repository;

import com.spoons.popparazzi.moim.dto.query.MoimSearchItemQuery;
import com.spoons.popparazzi.util.PaginationInfo;

import java.util.List;

public interface MoimSearchQueryRepository {

    List<MoimSearchItemQuery> searchMoims(String normalizedKeyword, PaginationInfo paginationInfo);

    long countSearchMoims(String normalizedKeyword);
}