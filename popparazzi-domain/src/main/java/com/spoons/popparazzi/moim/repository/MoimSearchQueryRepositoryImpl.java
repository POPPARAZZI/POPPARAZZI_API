package com.spoons.popparazzi.moim.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spoons.popparazzi.common.YesNo;
import com.spoons.popparazzi.member.entity.QMember;
import com.spoons.popparazzi.moim.dto.query.MoimSearchItemQuery;
import com.spoons.popparazzi.moim.entity.QMoim;
import com.spoons.popparazzi.moim.entity.QMoimMemberMapping;
import com.spoons.popparazzi.popup.entity.QPopup;
import com.spoons.popparazzi.util.PaginationInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MoimSearchQueryRepositoryImpl implements MoimSearchQueryRepository {

    private final JPAQueryFactory queryFactory;

    private static final QMoim moim = QMoim.moim;
    private static final QMember member = QMember.member;
    private static final QPopup popup = QPopup.popup;
    private static final QMoimMemberMapping moimMemberMapping = QMoimMemberMapping.moimMemberMapping;

    @Override
    public List<MoimSearchItemQuery> searchMoims(String normalizedKeyword, PaginationInfo paginationInfo) {
        return queryFactory
                .select(Projections.constructor(
                        MoimSearchItemQuery.class,
                        moim.moimCode,
                        moim.popupCode,
                        moim.title,
                        popupAddress(),
                        moim.leaderMemberCode,
                        member.nickname,
                        moim.maxParticipants
                ))
                .from(moim)
                .join(member).on(member.memberCode.eq(moim.leaderMemberCode))
                .join(popup).on(popup.popupCode.eq(moim.popupCode))
                .where(
                        moim.deleteYn.eq(YesNo.NO),
                        keywordContains(normalizedKeyword),
                        isOpenMoim()
                )
                .orderBy(moim.regDt.desc())
                .offset(paginationInfo.getFirstRecordIndex())
                .limit(paginationInfo.getRecordCountPerPage())
                .fetch();
    }

    @Override
    public long countSearchMoims(String normalizedKeyword) {
        Long count = queryFactory
                .select(moim.count())
                .from(moim)
                .where(
                        moim.deleteYn.eq(YesNo.NO),
                        keywordContains(normalizedKeyword),
                        isOpenMoim()
                )
                .fetchOne();

        return count == null ? 0L : count;
    }

    /**
     * 열린 모임 조건
     * 1. 모임 날짜가 현재보다 미래
     * 2. 승인 완료 + 참여중인 인원 수가 최대 정원보다 작음
     */
    private BooleanExpression isOpenMoim() {
        return moim.date.after(LocalDateTime.now())
                .and(hasAvailableSlots());
    }

    /**
     * 현재 승인 완료 참여자 수 < 최대 정원
     */
    private BooleanExpression hasAvailableSlots() {
        return JPAExpressions
                .select(moimMemberMapping.count())
                .from(moimMemberMapping)
                .where(
                        moimMemberMapping.id.moimCode.eq(moim.moimCode),
                        moimMemberMapping.joinYn.eq(YesNo.YES),
                        moimMemberMapping.isApproved.isTrue()
                )
                .lt(moim.maxParticipants.longValue());
    }

    /**
     * 제목 또는 내용에 검색어 포함
     */
    private BooleanExpression keywordContains(String normalizedKeyword) {
        if (normalizedKeyword == null || normalizedKeyword.isBlank()) {
            return null;
        }

        String likeKeyword = "%" + normalizedKeyword + "%";

        return normalizedTitle().like(likeKeyword)
                .or(normalizedBody().like(likeKeyword));
    }

    /**
     * 제목 공백 제거 후 비교
     */
    private StringExpression normalizedTitle() {
        return Expressions.stringTemplate(
                "replace({0}, ' ', '')",
                moim.title
        );
    }

    /**
     * 내용 공백 제거 후 비교
     */
    private StringExpression normalizedBody() {
        return Expressions.stringTemplate(
                "replace({0}, ' ', '')",
                moim.body
        );
    }

    /**
     * 검색 카드 지역 표시용
     * 예: 서울 성동구
     */
    private StringExpression popupAddress() {
        return Expressions.stringTemplate(
                "concat({0}, ' ', {1})",
                popup.addrSido,
                popup.addrSigungu
        );
    }
}