package com.spoons.popparazzi.auth.entity;


import com.spoons.popparazzi.auth.entity.enums.MemberRole;
import com.spoons.popparazzi.auth.entity.enums.MemberStatus;
import com.spoons.popparazzi.auth.entity.enums.SnsType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tbl_member_master")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {


    @Id
    @Size(max = 22)
    @Column(name = "tmm_code", nullable = false, length = 22)
    @Setter
    private String memberCode;

    @Size(max = 50)
    @NotNull
    @Column(name = "tmm_id", nullable = false, length = 50)
    private String memberId;

    @Size(max = 255)
    @NotNull
    @Column(name = "tmm_pwd", nullable = false, length = 50)
    private String memberPwd;

    @Size(max = 255)
    @Column(name = "tmm_phone", length = 50)
    private String phone;

    @Size(max = 15)
    @Column(name = "tmm_name", length = 15)
    private String name;

    @Size(max = 50)
    @Column(name = "tmm_nickname", length = 50)
    private String nickName;

    @Size(max = 100)
    @NotNull
    @Column(name = "tmm_email", nullable = false, length = 100)
    private String email;

    @Size(max = 255)
    @Column(name = "tmm_token")
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "tmm_sns_type")
    private SnsType snsType;

    @Enumerated(EnumType.STRING)
    @Column(name = "tmm_role")
    private MemberRole role;

    @ColumnDefault("'N'::bpchar")
    @Column(name = "tmm_auth_yn", nullable = false)
    private String authYn = "N";

    @CreationTimestamp
    @Column(name = "tmm_reg_dt", nullable = false)
    private Instant regDt;

    @Size(max = 255)
    @Column(name = "tmm_profile_url")
    private String profileUrl;

    @Size(max = 50)
    @NotNull
    @Column(name = "tmm_uuid", nullable = false, length = 50)
    private String memberUuid;

    @NotNull
    @Column(name = "tmm_gender", nullable = false)
    private String gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "tmm_status", nullable = false, length = 20)
    private MemberStatus status;

    public Member(String memberId, String memberPwd, String email, String nickName, String gender, SnsType snsType, UUID uuid) {
        this.memberId = memberId;
        this.memberPwd = memberPwd;
        this.email = email;
        this.nickName = nickName;
        this.gender = gender;
        this.snsType = snsType;
        this.memberUuid = uuid.toString();
        this.role = MemberRole.USER;
        this.status = MemberStatus.ACTIVE;
    }

    public void updateToken(String token) {
        this.token = token;
    }
}
