package com.spoons.popparazzi.auth.repository;


import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.hs.domain.modules.charge.domain.model.QCharge;
import kr.co.hs.domain.modules.charge.infrastructure.dto.ChargeResponse;
import kr.co.hs.domain.modules.mark.domain.model.QMark;
import kr.co.hs.domain.modules.mark.infrastructure.dto.MarkResponse;
import kr.co.hs.domain.modules.markVerify.domain.model.QMarkVerify;
import kr.co.hs.domain.modules.member.domain.constant.Status;
import kr.co.hs.domain.modules.member.infrastructure.dto.response.AdminDetail;
import kr.co.hs.domain.modules.member.infrastructure.dto.response.MemberDetail;
import kr.co.hs.domain.modules.member.infrastructure.dto.response.MemberResponseDTO;
import kr.co.hs.domain.modules.refund.infrastructure.dto.ChargeRefund;
import kr.co.hs.domain.modules.trial.infrastructure.dto.MemberSearch;
import kr.co.hs.util.enc.EncUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

import static kr.co.hs.domain.modules.charge.domain.model.QCharge.charge;
import static kr.co.hs.domain.modules.contentMeta.domain.model.QContentMeta.contentMeta;
import static kr.co.hs.domain.modules.distributor.domain.model.QDistributor.distributor;
import static kr.co.hs.domain.modules.mark.domain.model.QMark.mark;
import static kr.co.hs.domain.modules.markVerify.domain.model.QMarkVerify.markVerify;
import static kr.co.hs.domain.modules.member.domain.model.QMember.member;
import static kr.co.hs.domain.modules.point.domain.model.QPoint.point;
import static kr.co.hs.domain.modules.pointTransaction.domain.constant.PointTransactionType.POINT_USE;
import static kr.co.hs.domain.modules.pointTransaction.domain.model.QPointTransaction.pointTransaction;
import static kr.co.hs.domain.modules.refund.domain.model.QChargeRefund.chargeRefund;
import static kr.co.hs.domain.util.constant.paging.QueryDslUtils.getRowNumber;


@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<MemberResponseDTO> findMemberList(Pageable pageable, BooleanExpression predicate) {
        long offset = pageable.getOffset();
        long limit = pageable.getPageSize();

        JPAQuery<MemberResponseDTO> query = queryFactory
                .select(Projections.constructor(MemberResponseDTO.class,
                        member.memberCode,
                        member.memberId,
                        member.email,
                        member.name,
                        member.birth,
                        member.gender,
                        member.status,
                        member.createAt,
                        point.pointBalance.coalesce(BigDecimal.ZERO), // 현재 포인트 잔액이 없으면 0
                        pointTransaction.pointTransactionAmount.sum().coalesce(BigDecimal.ZERO) // 사용된 포인트 합계가 없으면 0
                ))
                .from(member)
                .leftJoin(point)
                .on(member.memberCode.eq(point.memberCode))
                .leftJoin(pointTransaction)
                .on(member.memberCode.eq(pointTransaction.memberCode)
                        .and(pointTransaction.pointTransactionType.eq(POINT_USE))) // 포인트 사용 내역만 조인
                .where(predicate);

        return query
                .groupBy(member.memberCode)
                .orderBy(member.createAt.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }


    public MemberSearch findByMemberCode(String memberCode) {

        Tuple result = queryFactory.select(
                member.memberCode,
                member.memberId,
                member.name,
                member.phoneNumber
        )
                .from(member)
                .where(member.memberCode.eq(memberCode).and(member.status.eq(Status.ACTIVITY)))
                .fetchOne();

        if (result == null) {
            return null; // 조회 결과가 없을 경우 null 처리
        }

        String phoneNumber = result.get(member.phoneNumber);

        return new MemberSearch(
                result.get(member.memberCode),
                result.get(member.memberId),
                result.get(member.name),
                decryptedPhoneNumber(phoneNumber)
                );
    }

    private String decryptedPhoneNumber(String phoneNumber) {

        String decryptedPhoneNumber = "";

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            try {
                decryptedPhoneNumber = EncUtils.decrypt(phoneNumber, "signup"); // 복호화 로직
            } catch (Exception e) {
                throw new RuntimeException(e); // 예외 처리
            }
        }

        return decryptedPhoneNumber;
    }

    public MemberDetail getMemberDetail(String memberCode) {
        List<ChargeResponse> chargeResponses = fetchChargeResponses(memberCode);
        List<MarkResponse> markResponses = fetchMarkResponses(memberCode);
        List<MarkResponse> markVerifyResponses = fetchMarkVerifyResponses(memberCode);

        return queryFactory.select(Projections.constructor(MemberDetail.class,
                        member.createAt,
                        member.memberId,
                        member.name,
                        member.status,
                        point.pointBalance.coalesce(BigDecimal.ZERO), // 포인트 잔액 처리
                        new CaseBuilder()
                                .when(pointTransaction.pointTransactionType.eq(POINT_USE)) // 조건 추가
                                .then(pointTransaction.pointTransactionAmount.abs())
                                .otherwise(BigDecimal.ZERO)
                                .sum()
                                .coalesce(BigDecimal.ZERO), // 사용 포인트 처리
                        Expressions.constant(chargeResponses), // 구매 내역 전달
                        Expressions.constant(markResponses), // 마크 내역 전달
                        Expressions.constant(markVerifyResponses) // 검출 내역 전달
                ))
                .from(member)
                .leftJoin(point).on(member.memberCode.eq(point.memberCode))
                .leftJoin(pointTransaction).on(member.memberCode.eq(pointTransaction.memberCode))
                .where(member.memberCode.eq(memberCode))
                .fetchOne();
    }

    // 구매 내역 조회
    private List<ChargeResponse> fetchChargeResponses(String memberCode) {
         return queryFactory.select(Projections.constructor(ChargeResponse.class,
                        getRowNumber(new PathBuilder<>(QCharge.class, "charge"), "createAt"),
                        charge.chargeId,
                        charge.status,
                        charge.paymentAmount,
                        charge.remainingPoint,
                        charge.usedPoint,
                        charge.createAt,
                        Projections.constructor(ChargeRefund.class,
                                chargeRefund.refundId,
                                chargeRefund.status,
                                chargeRefund.refundReason,
                                chargeRefund.createAt)))
                .from(charge)
                .leftJoin(chargeRefund).on(chargeRefund.chargeId.eq(charge.chargeId))
                .leftJoin(member).on(member.memberCode.eq(charge.memberCode))
                .where(charge.memberCode.eq(memberCode))
                .groupBy(charge.chargeId)
                .orderBy(charge.createAt.desc())
                .fetch();

    }

    // 마크 생성 내역 조회
    private List<MarkResponse> fetchMarkResponses(String memberCode) {
        return queryFactory.select(Projections.constructor(MarkResponse.class,
                        getRowNumber(new PathBuilder<>(QMark.class, "mark"), "createAt"),
                        contentMeta.title,
                        distributor.name,
                        contentMeta.fileType,
                        mark.createAt,
                        mark.fileName,
                        mark.markMsg))
                .from(mark)
                .leftJoin(contentMeta).on(contentMeta.contentMetaId.eq(mark.contentMetaId))
                .leftJoin(distributor).on(distributor.distributorId.eq(mark.distributorId))
                .where(mark.memberCode.eq(memberCode))
                .orderBy(mark.createAt.desc())
                .fetch();

    }

    // 마크 검출 내역 조회
    private List<MarkResponse> fetchMarkVerifyResponses(String memberCode) {
        return queryFactory.select(Projections.constructor(MarkResponse.class,
                        getRowNumber(new PathBuilder<>(QMarkVerify.class, "markVerify"), "createAt"),
                        contentMeta.title,
                        distributor.name,
                        contentMeta.fileType,
                        markVerify.createAt,
                        mark.fileName,
                        markVerify.verifyInfo))
                .from(markVerify)
                .leftJoin(mark).on(mark.markId.eq(markVerify.markId))
                .leftJoin(distributor).on(distributor.distributorId.eq(mark.distributorId))
                .leftJoin(contentMeta).on(contentMeta.contentMetaId.eq(mark.contentMetaId))
                .where(markVerify.memberCode.eq(memberCode))
                .orderBy(markVerify.createAt.desc())
                .fetch();


    }



    public AdminDetail getDetailAdmin(String memberCode) {

        return queryFactory.select(Projections.constructor(AdminDetail.class,
                member.memberId,
                member.name,
                member.email,
                member.status))
                .from(member)
                .where(member.memberCode.eq(memberCode))
                .fetchOne();
    }



}
