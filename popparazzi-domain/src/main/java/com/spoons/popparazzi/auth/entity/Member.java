package com.spoons.popparazzi.auth.entity;


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
@Table(name = "TBL_MEMBER_MASTER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {


    @Id
    @Size(max = 22)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TMM_CODE", nullable = false, length = 22)
    private String memberCode;

    @Size(max = 50)
    @NotNull
    @Column(name = "TMM_ID", nullable = false, length = 50)
    private String memberId;

    @Size(max = 50)
    @NotNull
    @Column(name = "TMM_PWD", nullable = false, length = 50)
    private String memberPwd;

    @Size(max = 50)
    @Column(name = "TMM_PHONE", length = 50)
    private String phone;

    @Size(max = 15)
    @Column(name = "TMM_NAME", length = 15)
    private String name;

    @Size(max = 50)
    @Column(name = "TMM_NICKNAME", length = 50)
    private String nickName;

    @Size(max = 100)
    @NotNull
    @Column(name = "TMM_EMAIL", nullable = false, length = 100)
    private String email;

    @Size(max = 255)
    @Column(name = "TMM_TOKEN")
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "TMM_SNS_TYPE")
    private SnsType snsType;

    @NotNull
    @ColumnDefault("'N'::bpchar")
    @Column(name = "TMM_AUTH_YN", nullable = false)
    private String authYn;

    @NotNull
    @CreationTimestamp
    @Column(name = "TMM_REG_DT", nullable = false)
    private Instant regDt;

    @Size(max = 255)
    @Column(name = "TMM_PROFILE_URL")
    private String profileUrl;

    @Size(max = 50)
    @NotNull
    @Column(name = "TMM_UUID", nullable = false, length = 50)
    private String memberUuid;

    @NotNull
    @Column(name = "TMM_GENDER", nullable = false)
    private String gender;

    public Member(String memberId, String memberPwd, String email, String nickName, String gender, SnsType snsType, UUID uuid) {
        this.memberId = memberId;
        this.memberPwd = memberPwd;
        this.email = email;
        this.nickName = nickName;
        this.gender = gender;
        this.snsType = snsType;
        this.memberUuid = uuid.toString();
    }


    public void assignCode(String code) {
        this.memberCode = code;
    }

}
