package com.spoons.popparazzi.auth.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import kr.co.hs.domain.modules.member.domain.constant.ProviderType;
import kr.co.hs.domain.modules.member.domain.constant.RoleType;
import kr.co.hs.domain.modules.member.domain.constant.Status;
import kr.co.hs.domain.modules.member.domain.model.Member;
import kr.co.hs.domain.modules.member.infrastructure.dto.request.MemberUpdate;
import kr.co.hs.domain.modules.member.infrastructure.dto.response.AdminDetail;
import kr.co.hs.domain.modules.member.infrastructure.dto.response.MemberDetail;
import kr.co.hs.domain.modules.member.infrastructure.dto.response.MemberResponseDTO;
import kr.co.hs.domain.modules.member.infrastructure.platform.MemberJpaRepository;
import kr.co.hs.domain.modules.member.infrastructure.platform.MemberQueryRepository;
import kr.co.hs.domain.modules.memberAuth.application.EmailService;
import kr.co.hs.domain.modules.memberAuth.domain.model.VerifyCode;
import kr.co.hs.domain.modules.memberAuth.infrastructure.dto.EmailMessage;
import kr.co.hs.domain.modules.memberAuth.infrastructure.platform.VerifyCodeRepository;
import kr.co.hs.domain.modules.point.domain.model.Point;
import kr.co.hs.domain.modules.point.infrastructure.platform.PointJpaRepository;
import kr.co.hs.domain.modules.pointTransaction.infrastructure.platform.PointTransactionQueryRepository;
import kr.co.hs.domain.util.constant.paging.PagingUtils;
import kr.co.hs.util.exception.MarkException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static kr.co.hs.domain.modules.member.domain.constant.Status.ACTIVITY;
import static kr.co.hs.domain.modules.member.domain.model.QMember.member;
import static kr.co.hs.util.exception.type.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceEx {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberQueryRepository queryRepository;
    private final VerifyCodeRepository verifyCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PointTransactionQueryRepository pointTransactionQueryRepository;
    private final PointJpaRepository pointJpaRepository;
    private final PagingUtils pagingUtils;

    /* 아이디 찾기 */
    @Transactional(readOnly = true)
    public Member searchId( String email, String memberName) {

        final Member member = memberJpaRepository.findByEmailAndName(email, memberName);

        if(member != null) {
            Status memberStatus = member.getStatus();

            if(memberStatus.equals(ACTIVITY)){
                return member;
            }else {
                throw new MarkException(NOT_FOUND_MEMBER_NON_STATUS);
            }

        } else {
            throw new MarkException(USER_NOT_FOUND);
        }
    }

    public Member searchMember(String memberId, String memberName) {


        Member member = memberJpaRepository.findByMemberIdAndName(memberId, memberName);

        if(member != null) {

            Status memberStatus = member.getStatus();

            if(memberStatus.equals(ACTIVITY)){
                return member;
            }else {
                throw new MarkException(NOT_FOUND_MEMBER_NON_STATUS);
            }

        }else {
            throw new MarkException(USER_NOT_FOUND);
        }


    }

    /* id찾기 이메일 보내기 */
    @Transactional
    public void sendForgotIdEmail(Member member) {

        String to = member.getEmail();
        String subject = "[UnMark] 아이디찾기 인증 코드 안내 이메일 입니다.";

        sendVerifyCode(to, subject);

    }


    /* 비밀번호 변경 코드 전송 */
    @Transactional
    public void sendForgotPasswordEmail(String email) {
        String subject = "[UnMark] 비밀번호 인증 코드 안내 이메일 입니다.";

        sendVerifyCode(email, subject);
    }


    @Transactional
    public String updatePassword(Member member) {
        String newPaw = createRandomPwd(10);

        member.updatePwd(newPaw, passwordEncoder);
        memberJpaRepository.save(member);
        return newPaw;
    }

    /* 아이디 중복 체크 */
    @Transactional(readOnly = true)
    public Boolean duplicateId(String memberId) {
        return memberJpaRepository.existsByMemberId(memberId);
    }

    /* 이메일 인증 */
    @Transactional
    public void sendCodeToEmail(String email) {

        boolean hasEmail = checkDuplicatedEmail(email);

        if(hasEmail) {
            String subject = "[UnMark] 이메일 인증 번호";
            sendVerifyCode(email, subject);
        } else {
            throw new MarkException(HAS_EMAIL);
        }

    }

    private void sendVerifyCode(String to, String subject) {
        String authCode = "";

        VerifyCode alreadySendCode = verifyCodeRepository.findCodeByEmail(to);

        if (alreadySendCode != null) {
            verifyCodeRepository.deleteByEmail(to);
        }

        authCode = this.createCode();

        VerifyCode verifyCode = new VerifyCode();
        verifyCode.setEmail(to);
        verifyCode.setCode(authCode);
        verifyCodeRepository.save(verifyCode);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(to)
                .subject(subject)
                .message("인증번호는 " + authCode + "입니다.")
                .build();

        try {
            emailService.sendMail(emailMessage);
        } catch (MarkException e) {

            log.error("이메일 전송 실패: {}", e.getMessage());

            throw new MarkException(ERROR_SEND_EMAIL);
        } catch (Exception e) {
            // 다른 예외 처리
            log.error("알 수 없는 오류 발생: {}", e.getMessage());
            throw new RuntimeException("알 수 없는 오류가 발생했습니다. 나중에 다시 시도해주세요.", e);
        }
    }


    /* 아이디 * 처리 */
    public static String maskId(String id) {
        String prefix = id.substring(0, id.length() - 3); // 뒤 3글자를 제외한 부분
        return prefix + "*".repeat(3);
    }

    /* 이메일 검증 */
    @Transactional
    public void verifiedCode(VerifyCode request) {
        String email = request.getEmail();
        String code = request.getCode();

        VerifyCode verifyCode = verifyCodeRepository.findByCode(code);

        if(verifyCode == null) {
            throw new MarkException(INVALID_CODE);
        }

        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(verifyCode.getIssueDate());
        calendar.add(Calendar.MINUTE, 30);
        Date thirtyMinutesAfterIssueDate = calendar.getTime();

        if(now.before(thirtyMinutesAfterIssueDate)) {
            if(email.equals(verifyCode.getEmail())) {
                verifyCodeRepository.deleteByEmail(email);
            } else {
                throw new MarkException(INVALID_EMAIL);
            }
        } else  {
            throw new MarkException(EXPIRED_CODE_TIME);
        }


    }

    /* 사용중인 이메일인지 확인 */
    private boolean checkDuplicatedEmail(String email) {
        Optional<Member> member = memberJpaRepository.findByEmail(email);
        if(member.isPresent()) {
            throw new MarkException(HAS_EMAIL);
        }
        return true;
    }

    /* 인증번호  숫자로만 */
    private String createCode() {
        try {
            Random random = SecureRandom.getInstanceStrong();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                builder.append(random.nextInt(10));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            log.debug("CodeGenerator.createCode() exception occurred", e);
            throw new MarkException(NO_SUCH_ALGORITHM);
        }
    }

    /* 인증 코드 문자포함 */
    public String createRandomPwd(int length) {
        try {
            Random random = SecureRandom.getInstanceStrong();
            StringBuilder builder = new StringBuilder();
            String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%"; // 대문자, 소문자, 숫자, 특수문자

            for (int i = 0; i < length; i++) {
                int index = random.nextInt(characters.length()); // 0부터 characters.length() - 1까지의 랜덤 인덱스
                builder.append(characters.charAt(index)); // 랜덤 인덱스에 해당하는 문자 추가
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new MarkException(NO_SUCH_ALGORITHM);
        }
    }

    /* 회원 리스트 조회 */
    public PageImpl<MemberResponseDTO> findAllMember(Optional<Integer> page, Optional<Integer> pageSize, Optional<String> searchTerm, Optional<LocalDate> startDate, Optional<LocalDate> endDate, Optional<Status> status) {

        Pageable pageRequest = pagingUtils.createPageRequest(page, pageSize);

        BooleanExpression predicate = member.authority.eq(RoleType.MEMBER);
        // searchTerm 조건 추가
        if (searchTerm.isPresent()) {
            predicate = predicate.and(   member.memberId.contains(searchTerm.get())
                    .or(member.name.contains(searchTerm.get())));
        }

        // startDate와 endDate 조건 추가
        if (startDate.isPresent() && endDate.isPresent()) {

            LocalDateTime startDateTime = pagingUtils.getStartDateTime(startDate);
            LocalDateTime endDateTime = pagingUtils.getEndDateTime(endDate);

            predicate = predicate.and(member.createAt.between(startDateTime, endDateTime));
        }

        // status 조건 추가
        if (status.isPresent()) {
            predicate = predicate.and(member.status.eq(status.get()));
        }

        long total = pagingUtils.getTotalCount(member, predicate);

        List<MemberResponseDTO> list = queryRepository.findMemberList(
                pageRequest,
               predicate
        );

        return new PageImpl<>(list, pageRequest, total);
    }



    /* 회원 상세 조회 */
    public MemberResponseDTO getMyProfile(String memberCode) throws Exception {

        // 회원 조회
        final Member member = memberJpaRepository.findByMemberCode(memberCode)
                .orElseThrow( () -> new MarkException(USER_NOT_FOUND));

        Point point = pointJpaRepository.findByMemberCode(memberCode);

        BigDecimal hasPoint = null;
        BigDecimal usedPoint = null;
        
        if(point != null) {
             hasPoint = point.getPointBalance();
             usedPoint = pointTransactionQueryRepository.findUsedPointByMemberCode(memberCode);
        }
      

        return MemberResponseDTO.of(member,  hasPoint, usedPoint);
    }

    /* 회원 정보 수정 */
    @Transactional
    public void profileUpdate(MemberUpdate request, String memberCode) throws Exception {
        // 회원 조회
        final Member member = memberJpaRepository.findByMemberCode(memberCode)
                .orElseThrow( () -> new MarkException(USER_NOT_FOUND));
        // 회원 정보 업데이트
        member.profileUpdate(request, passwordEncoder);

        memberJpaRepository.save(member);
    }

    /* 이용 상태 변경 */
    @Transactional
    public void statusChange(List<String> selectedMembers, Status status) {

        List<Member> members = memberJpaRepository.findByMemberCodeIn(selectedMembers)
                .orElseThrow( () -> new MarkException(USER_NOT_FOUND));

        for (Member member : members) {
            member.statusChange(status);
        }

        memberJpaRepository.saveAll(members);
    }

    /* 관리자 조회 */
    public PageImpl<MemberResponseDTO> findAllAdmin(Optional<Integer> page, Optional<Integer> pageSize,  Optional<String> searchTerm, Optional<Status> status) {

        Pageable pageRequest = pagingUtils.createPageRequest(page, pageSize);

        BooleanExpression predicate = member.authority.eq(RoleType.ADMIN);
        // searchTerm 조건 추가
        if (searchTerm.isPresent()) {
            predicate = predicate.and(   member.memberId.contains(searchTerm.get())
                    .or(member.name.contains(searchTerm.get())));
        }

        // status 조건 추가
        if (status.isPresent()) {
            predicate = predicate.and(member.status.eq(status.get()));
        }

        long total = pagingUtils.getTotalCount(member, predicate);

        List<MemberResponseDTO> list = queryRepository.findMemberList(
                pageRequest,
                predicate
        );

        return new PageImpl<>(list, pageRequest, total);
    }

    public AdminDetail detailAdmin(String memberCode) {
        return queryRepository.getDetailAdmin(memberCode);
    }

    public MemberDetail getMemberDetail(String memberCode) {
        return queryRepository.getMemberDetail(memberCode);
    }

    /* 회원 탈퇴 */
    public void signOut(String memberCode) {

        Member member = memberJpaRepository.findByMemberCode(memberCode).orElseThrow( () -> new MarkException(USER_NOT_FOUND));
        member.signOut();
        memberJpaRepository.save(member);
    }

    public String getProvider(String memberCode) {
        Member member = memberJpaRepository.findByMemberCode(memberCode).orElseThrow( () -> new MarkException(USER_NOT_FOUND));

        ProviderType provider = member.getProvider();

        return provider.toString();
    }
}
