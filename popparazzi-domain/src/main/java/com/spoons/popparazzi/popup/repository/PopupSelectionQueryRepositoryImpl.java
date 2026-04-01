package com.spoons.popparazzi.popup.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spoons.popparazzi.popup.dto.query.PopupSelectionItemQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.spoons.popparazzi.popup.entity.QPopup.popup;

@Repository
@RequiredArgsConstructor
public class PopupSelectionQueryRepositoryImpl implements PopupSelectionQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    // 1. 모임 생성시 필요한 팝업 리스트(신규순)
    public List<PopupSelectionItemQuery> findNewestSelections(int limit) {
        return queryFactory
                .select(Projections.constructor(
                        PopupSelectionItemQuery.class,
                        popup.popupCode,
                        popup.title,
                        popup.addrSido,
                        popup.addrSigungu,
                        popup.startDt,
                        popup.endDt
                ))
                .from(popup)
                .where(popup.endDt.goe(LocalDateTime.now()))
                .orderBy(popup.regDt.desc())
                .limit(limit)
                .fetch();
    }
}
