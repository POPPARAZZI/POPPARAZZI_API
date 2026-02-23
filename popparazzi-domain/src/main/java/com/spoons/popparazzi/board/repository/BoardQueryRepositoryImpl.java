package com.spoons.popparazzi.board.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spoons.popparazzi.board.dto.query.HotMoimBoardItemQuery;
import com.spoons.popparazzi.board.enums.BoardType;
import com.spoons.popparazzi.common.YesNo;
import com.spoons.popparazzi.like.enums.LikeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.spoons.popparazzi.board.entity.QBoardMaster.boardMaster;
import static com.spoons.popparazzi.like.entity.QLikeMapping.likeMapping;

@Repository
@RequiredArgsConstructor
public class BoardQueryRepositoryImpl implements BoardQueryRepository {

    private static final long RESULT_LIMIT = 4;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<HotMoimBoardItemQuery> findHotMoimBoards(LocalDateTime from, LocalDateTime to) {

        NumberExpression<Long> likeCount = likeMapping.id.count();

        return queryFactory
                .select(Projections.constructor(
                        HotMoimBoardItemQuery.class,
                        boardMaster.boardCode,
                        boardMaster.title,
                        likeCount
                ))
                .from(boardMaster)
                .leftJoin(likeMapping).on(
                        likeMapping.targetCode.eq(boardMaster.boardCode),
                        likeMapping.type.eq(LikeType.R),
                        likeMapping.createdAt.between(from, to)
                )
                .where(
                        boardMaster.type.eq(BoardType.M),
                        boardMaster.deleteYn.eq(YesNo.NO)
                )
                .groupBy(boardMaster.boardCode, boardMaster.title)
                .orderBy(
                        likeCount.desc(),
                        boardMaster.regDt.desc()
                )
                .limit(RESULT_LIMIT)
                .fetch();
    }
}