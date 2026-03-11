package com.spoons.popparazzi.moim.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spoons.popparazzi.common.YesNo;
import com.spoons.popparazzi.member.entity.QMember;
import com.spoons.popparazzi.moim.dto.query.MoimApplyInfoQuery;
import com.spoons.popparazzi.moim.dto.query.MoimDetailQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.spoons.popparazzi.member.entity.QMember.member;
import static com.spoons.popparazzi.moim.entity.QMoim.moim;

@Repository
@RequiredArgsConstructor
public class MoimQueryRepositoryImpl implements MoimQueryRepository {

    private final JPAQueryFactory queryFactory;

    // 1. 모임 상세 조회
    @Override
    public MoimDetailQuery findMoimDetail(String moimCode) {
        QMember member = QMember.member;

        return queryFactory
                .select(Projections.constructor(
                        MoimDetailQuery.class,
                        moim.moimCode,
                        moim.title,
                        moim.body,
                        moim.date,
                        moim.maxParticipants,
                        moim.leaderMemberCode,
                        member.profileUrl
                ))
                .from(moim)
                .leftJoin(member).on(member.memberCode.eq(moim.leaderMemberCode))
                .where(
                        moim.moimCode.eq(moimCode),
                        moim.deleteYn.eq(YesNo.NO)
                )
                .fetchOne();
    }

    // 2. 모임 신청 화면 조회
    @Override
    public Optional<MoimApplyInfoQuery> findApplyInfoByMoimCode(String moimCode) {
        return Optional.ofNullable(
                queryFactory
                        .select(Projections.constructor(
                                MoimApplyInfoQuery.class,
                                member.profileUrl,
                                member.nickname,
                                moim.preQuestion
                        ))
                        .from(moim)
                        .join(member).on(moim.leaderMemberCode.eq(member.memberCode))
                        .where(
                                moim.moimCode.eq(moimCode),
                                moim.deleteYn.eq(YesNo.NO)
                        )
                        .fetchOne()
        );
    }
}
