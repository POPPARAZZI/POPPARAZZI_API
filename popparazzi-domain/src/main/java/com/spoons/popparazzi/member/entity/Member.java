package com.spoons.popparazzi.member.entity;

import com.spoons.popparazzi.common.YesNo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_member_master")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @Column(name = "tmm_code", length = 22)
    private String memberCode;

    @Column(name = "tmm_id", nullable = false, length = 50)
    private String loginId;

    @Column(name = "tmm_pwd", nullable = false, length = 50)
    private String password;

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
    private String snsType;

    @Convert(converter = com.spoons.popparazzi.common.YesNoConverter.class)
    @Column(name = "tmm_auth_yn", nullable = false, length = 1)
    private YesNo authYn;

    @Column(name = "tmm_reg_dt", nullable = false)
    private LocalDateTime regDt;

    @Column(name = "tmm_profile_url", length = 255)
    private String profileUrl;

    @Column(name = "tmm_uuid", nullable = false, length = 50)
    private String uuid;

    @Column(name = "tmm_gender", nullable = false, length = 1)
    private String gender;

    @Column(name = "tmm_bio", length = 160)
    private String bio;

    @Column(name = "tmm_cover_url", length = 255)
    private String coverUrl;
}