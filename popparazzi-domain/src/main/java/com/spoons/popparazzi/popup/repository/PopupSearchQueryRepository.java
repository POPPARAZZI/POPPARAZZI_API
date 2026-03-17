package com.spoons.popparazzi.popup.repository;

import com.spoons.popparazzi.popup.dto.query.PopupSearchMatchQuery;

import java.util.Optional;

public interface PopupSearchQueryRepository {

    // 검색어와 가장 일치하는 팝업 단건 조회
    Optional<PopupSearchMatchQuery> findBestMatch(String normalizedKeyword);
}