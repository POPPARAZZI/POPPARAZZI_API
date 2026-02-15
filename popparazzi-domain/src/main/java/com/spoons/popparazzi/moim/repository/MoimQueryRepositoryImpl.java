package com.spoons.popparazzi.moim.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spoons.popparazzi.moim.dto.query.NewestMoimItemQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.spoons.popparazzi.moim.entity.QMoim.moim;

@Repository
@RequiredArgsConstructor
public class MoimQueryRepositoryImpl implements MoimQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<NewestMoimItemQuery> findNewestForMain(Pageable pageable) {
        return queryFactory
                .select(Projections.constructor(
                        NewestMoimItemQuery.class,
                        moim.moimCode,
                        moim.popupCode,
                        moim.title,
                        moim.date,
                        moim.maxParticipants
                ))
                .from(moim)
                .where(moim.deleteYn.eq("N"))
                .orderBy(moim.regDt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }
}
