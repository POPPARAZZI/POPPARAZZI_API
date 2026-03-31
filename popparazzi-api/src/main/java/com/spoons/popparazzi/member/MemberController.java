/*
package com.spoons.popparazzi.member;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;



@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    */
/* 아이디 찾기  - 인증코드 전송*//*

    @PostMapping("/find-id-verification")
    public ResponseEntity<HttpResData<String>> searchId (@RequestBody @Valid final MemberIdCheckRequest request) {

        Member member = memberService.searchId( request.email(), request.memberName() );

        String provider = member.getProvider() == LOCAL ? "일반 회원" : String.valueOf(member.getProvider());

        if (member.getProvider().equals(LOCAL)) {
            memberService.sendForgotIdEmail(member);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(HttpResData.error(409, "회원님께서는 " + provider + "로 가입되어 있습니다." ));
        }

        return ResponseEntity.ok(HttpResData.success("이메일 전송이 완료되었습니다."));
    }

    */
/* 아이디 찾기 - 아이디 받기*//*

    @GetMapping("find-id")
    public ResponseEntity<HttpResData<String>> findId(@RequestParam @Valid final String email,
                                                      @RequestParam @Valid final String memberName) {
        Member member = memberService.searchId( email, memberName );
        String memberId = MemberService.maskId(member.getMemberId());
        return ResponseEntity.ok(HttpResData.success(memberId));
    }

    */
/* 비밀번호 찾기 *//*

    @GetMapping("/find-password-pre")
    public ResponseEntity<HttpResData<String>> findPasswordUrl(@RequestParam @Valid final String memberId,
                                                               @RequestParam @Valid final String memberName) {

        Member member = memberService.searchMember( memberId, memberName);
        ProviderType provider = member.getProvider();

        if (provider.equals(LOCAL)) {
            return ResponseEntity.ok(HttpResData.success("일치하는 회원이 있습니다."));
        }else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(HttpResData.error(409,"회원님께서는 " + provider + "로 가입되어 있습니다." ));
        }
    }

    @PostMapping("/find-password-verification")
    public ResponseEntity<HttpResData<String>> findPasswordVerification(@RequestBody @Valid final MemberIdCheckRequest email) {
        memberService.sendForgotPasswordEmail(email.email());
        return ResponseEntity.ok(HttpResData.success("인증 코드를 발송했습니다."));
    }

    @PostMapping("/change-password")
    public ResponseEntity<HttpResData<String>> findPassword(@RequestBody @Valid final MemberIdCheckRequest request) {
        Member member = memberService.searchMember( request.memberId(), request.memberName() );

        if(member != null) {

            String randomPwd = memberService.updatePassword(member);

            return ResponseEntity.ok(HttpResData.success(randomPwd));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(HttpResData.error(400, "일치하는 회원이 없습니다." ));
        }

    }

    */
/* 아이디 중복 검사 *//*

    @GetMapping("/duplicate-id")
    public ResponseEntity<HttpResData<String>> duplicateId (@RequestParam @Valid  String memberId) {
        boolean has = memberService.duplicateId( memberId );
        if(has) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(HttpResData.error(400, "일치하는 ID가 있습니다."));
        } else {
            return ResponseEntity.ok(HttpResData.success("사용 가능한 ID 입니다."));
        }
    }

    */
/* 이메일 인증 *//*

    @PostMapping("/emails/verification-requests")
    public ResponseEntity<HttpResData<?>> emailVerificationRequests(@RequestBody @Valid final MemberIdCheckRequest email) {
        try {
            memberService.sendCodeToEmail(email.email());
            return ResponseEntity.ok(HttpResData.success("이메일 인증 코드를 발송했습니다."));
        } catch (EmailException e) {
            // 이메일 전송 중 발생한 오류 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(HttpResData.error(500,e.getMessage()));
        }
    }

    */
/* 인증 코드 검증 *//*

    @PostMapping("/verifications")
    public ResponseEntity<HttpResData<?>> emailVerifications(@RequestBody @Valid final VerifyCode request) {
        memberService.verifiedCode(request);
        return ResponseEntity.ok(HttpResData.success("ok"));
    }

    */
/* 회원 조회 *//*

    @GetMapping("/member-list")
    public ResponseEntity<HttpResData<PageImpl<MemberResponseDTO>>> getMemberList(
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> pageSize,
            @RequestParam Optional<String> searchTerm,
            @RequestParam Optional<LocalDate> startDate,
            @RequestParam Optional<LocalDate> endDate,
            @RequestParam Optional<Status> status
    ) {
        PageImpl<MemberResponseDTO> result = memberService.findAllMember(page,pageSize, searchTerm, startDate, endDate, status);
        return ResponseEntity.ok(HttpResData.success(result));
    }

    */
/* 회원 상세 조회 *//*

    @GetMapping("/member-detail/{memberCode}")
    public ResponseEntity<HttpResData<MemberDetail>> getMemberDetail(@PathVariable String memberCode) {
        MemberDetail result = memberService.getMemberDetail(memberCode);
        return ResponseEntity.ok(HttpResData.success(result));
    }

    */
/* 내 정보 조회 *//*

    @GetMapping("/my-profile")
    public ResponseEntity<HttpResData<MemberResponseDTO>> myProfile() throws Exception {
        String memberCode = MemberData.build();
        MemberResponseDTO memberResponse = memberService.getMyProfile(memberCode);

        return ResponseEntity.ok(HttpResData.success(memberResponse));
    }

    */
/* 회원 정보 수정 *//*

    @PostMapping("/profile-update/{memberCode}")
    public ResponseEntity<HttpResData<?>> myProfileUpdate(@RequestBody MemberUpdate request, @PathVariable String memberCode) throws Exception {

        if(request.role().equals(RoleType.MEMBER)) {
            memberCode = MemberData.build();
        }

        memberService.profileUpdate(request, memberCode);

        return ResponseEntity.ok(HttpResData.success("정보가 수정되었습니다."));
    }

    */
/* 이용 상태 변경 *//*

    @PostMapping("/status-change")
    public ResponseEntity<HttpResData<String>> statusChange(@RequestBody StatusChangeVO statusChangeVO) {

        if(statusChangeVO.statusYn() == null ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(HttpResData.error(400,"이용상태는 null일 수 없습니다." ));
        }

        if(statusChangeVO.selectedMembers().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(HttpResData.error(400,"변경할 회원을 선택해주세요." ));
        }

        memberService.statusChange(statusChangeVO.selectedMembers(), statusChangeVO.statusYn());

        return ResponseEntity.ok(HttpResData.success("이용상태가 수정되었습니다."));
    }

    */
/* 관리자 조회 *//*

    @GetMapping("/admin-list")
    public ResponseEntity<HttpResData<PageImpl<MemberResponseDTO>>> getAdminList(
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> pageSize,
            @RequestParam Optional<String> searchTerm,
            @RequestParam Optional<Status> status
    ) {
        PageImpl<MemberResponseDTO> result = memberService.findAllAdmin(page,pageSize, searchTerm, status);
        return ResponseEntity.ok(HttpResData.success(result));
    }

    */
/* 관리자 상세 조회 *//*

    @GetMapping("/admin/{memberCode}")
    public ResponseEntity<HttpResData<AdminDetail>> getAdmin(@PathVariable String memberCode) {

        AdminDetail result = memberService.detailAdmin(memberCode);

        return ResponseEntity.ok(HttpResData.success(result));
    }

    */
/* 회원 탈퇴 *//*

    @PostMapping("/sign-out")
    public ResponseEntity<HttpResData<String>> signOut() {
        String memberCode = MemberData.build();

        memberService.signOut(memberCode);

        return ResponseEntity.ok(HttpResData.success("정상적으로 탈퇴 되었습니다."));
    }

    */
/* provider 체크 *//*

    @GetMapping("/provider-check")
    public ResponseEntity<HttpResData<String>> getProvider() {
        String memberCode = MemberData.build();
        String result = memberService.getProvider(memberCode);
        return ResponseEntity.ok(HttpResData.success(result));
    }


}
*/
