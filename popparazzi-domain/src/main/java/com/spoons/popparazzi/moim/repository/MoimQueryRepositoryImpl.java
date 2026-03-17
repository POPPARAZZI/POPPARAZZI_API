package com.spoons.popparazzi.moim.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spoons.popparazzi.common.YesNo;
import com.spoons.popparazzi.member.entity.QMember;
import com.spoons.popparazzi.moim.dto.query.MoimApplyInfoQuery;
import com.spoons.popparazzi.moim.dto.query.MoimDetailQuery;
import com.spoons.popparazzi.moim.dto.query.MoimParticipantProfileQuery;
import com.spoons.popparazzi.moim.dto.query.MoimParticipantQuery;
import com.spoons.popparazzi.moim.dto.query.main.MoimParticipantsCountQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.spoons.popparazzi.member.entity.QMember.member;
import static com.spoons.popparazzi.moim.entity.QMoim.moim;
import static com.spoons.popparazzi.moim.entity.QMoimMemberMapping.moimMemberMapping;

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

    // 3. 모임 참여자 조회
    @Override
    public List<MoimParticipantQuery> findParticipantsByMoimCode(String moimCode) {
        NumberExpression<Integer> leaderOrder = new CaseBuilder()
                .when(member.memberCode.eq(moim.leaderMemberCode)).then(1)
                .otherwise(0);

        BooleanExpression isLeader = member.memberCode.eq(moim.leaderMemberCode);

        return queryFactory
                .select(Projections.constructor(
                        MoimParticipantQuery.class,
                        member.memberCode,
                        member.nickname,
                        member.bio,
                        member.profileUrl,
                        isLeader
                ))
                .from(moimMemberMapping)
                .join(member).on(moimMemberMapping.id.memberCode.eq(member.memberCode))
                .join(moim).on(moimMemberMapping.id.moimCode.eq(moim.moimCode))
                .where(
                        moimMemberMapping.id.moimCode.eq(moimCode),
                        moimMemberMapping.joinYn.eq(YesNo.YES),
                        moim.deleteYn.eq(YesNo.NO)
                )
                .orderBy(
                        leaderOrder.desc(),
                        member.nickname.asc()
                )
                .fetch();
    }

    // 4. 여러 모임의 승인 완료 참여자 수 집계
    @Override
    public List<MoimParticipantsCountQuery> countApprovedParticipants(List<String> moimCodes) {

        return queryFactory
                .select(Projections.constructor(
                        MoimParticipantsCountQuery.class,
                        moimMemberMapping.id.moimCode,
                        moimMemberMapping.count()
                ))
                .from(moimMemberMapping)
                .where(
                        moimMemberMapping.id.moimCode.in(moimCodes),
                        moimMemberMapping.joinYn.eq(YesNo.YES),
                        moimMemberMapping.isApproved.isTrue()
                )
                .groupBy(moimMemberMapping.id.moimCode)
                .fetch();
    }

    // 5. 여러 모임의 참여자 프로필 URL 조회 (최대 2명)
    @Override
    public List<MoimParticipantProfileQuery> findParticipantProfiles(List<String> moimCodes) {
        if (moimCodes == null || moimCodes.isEmpty()) {
            return List.of();
        }

        NumberExpression<Integer> leaderOrder = new CaseBuilder()
                .when(member.memberCode.eq(moim.leaderMemberCode)).then(1)
                .otherwise(0);

        return queryFactory
                .select(Projections.constructor(
                        MoimParticipantProfileQuery.class,
                        moimMemberMapping.id.moimCode,
                        member.profileUrl
                ))
                .from(moimMemberMapping)
                .join(member).on(moimMemberMapping.id.memberCode.eq(member.memberCode))
                .join(moim).on(moimMemberMapping.id.moimCode.eq(moim.moimCode))
                .where(
                        moimMemberMapping.id.moimCode.in(moimCodes),
                        moimMemberMapping.joinYn.eq(YesNo.YES),
                        moimMemberMapping.isApproved.isTrue(),
                        moim.deleteYn.eq(YesNo.NO)
                )
                .orderBy(
                        moimMemberMapping.id.moimCode.asc(),
                        leaderOrder.desc(),
                        member.nickname.asc()
                )
                .fetch();
    }
}
