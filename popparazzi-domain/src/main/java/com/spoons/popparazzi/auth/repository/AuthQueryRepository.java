/*
package com.spoons.popparazzi.auth.repository;


import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;



@Repository
@RequiredArgsConstructor
public class AuthQueryRepository {

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

}
*/
