package com.spoons.popparazzi.member.entity;

import com.spoons.popparazzi.auth.entity.enums.MemberRole;
import com.spoons.popparazzi.auth.entity.enums.MemberStatus;
import com.spoons.popparazzi.auth.entity.enums.SnsType;
import com.spoons.popparazzi.common.YesNo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tbl_member_master")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @Column(name = "tmm_code", length = 22)
    private String memberCode;

    @Column(name = "tmm_id", nullable = false, length = 50)
    private String memberId;

    @Column(name = "tmm_pwd", nullable = false, length = 255)
    private String memberPwd;

    @Column(name = "tmm_phone", length = 50)
    private String phone;

    @Column(name = "tmm_name", length = 15)
    private String name;

    @Column(name = "tmm_nickname", length = 50)
    private String nickname;

    @Column(name = "tmm_email", nullable = false, length = 100)
    private String email;

    @Column(name = "tmm_token", length = 255)
    private String token;

    @Column(name = "tmm_sns_type", length = 1)
    private SnsType snsType;

    @Convert(converter = com.spoons.popparazzi.common.YesNoConverter.class)
    @Column(name = "tmm_auth_yn", nullable = false, length = 1)
    private YesNo authYn;

    @Column(name = "tmm_reg_dt", nullable = false)
    private LocalDateTime regDt;

    @Column(name = "tmm_profile_url", length = 255)
    private String profileUrl;

    @Column(name = "tmm_uuid", nullable = false, length = 50)
    private String memberUuid;

    @Column(name = "tmm_gender", nullable = false, length = 1)
    private String gender;

    @Column(name = "tmm_bio", length = 160)
    private String bio;

    @Column(name = "tmm_cover_url", length = 255)
    private String coverUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "tmm_role", length = 20)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "tmm_status", length = 20)
    private MemberStatus status;

    public Member(String memberId, String memberPwd, String email, String nickname, String gender, SnsType snsType, UUID uuid) {
        this.memberId = memberId;
        this.memberPwd = memberPwd;
        this.email = email;
        this.nickname = nickname;
        this.gender = gender;
        this.snsType = snsType;
        this.memberUuid = uuid.toString();
        this.authYn = YesNo.NO;
        this.role = MemberRole.USER;
        this.status = MemberStatus.ACTIVE;
    }

    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }

    public void updateToken(String token) {
        this.token = token;
    }
}