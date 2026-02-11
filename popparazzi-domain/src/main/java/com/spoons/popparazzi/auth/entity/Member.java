package com.spoons.popparazzi.auth.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "TBL_MEMBER_MASTER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
public class Member {


    @Id
    @Size(max = 22)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TMM_CODE", nullable = false, length = 22)
    private String tmmCode;

    @Size(max = 50)
    @NotNull
    @Column(name = "TMM_ID", nullable = false, length = 50)
    private String tmmId;

    @Size(max = 50)
    @NotNull
    @Column(name = "TMM_PWD", nullable = false, length = 50)
    private String tmmPwd;

    @Size(max = 50)
    @Column(name = "TMM_PHONE", length = 50)
    private String tmmPhone;

    @Size(max = 15)
    @Column(name = "TMM_NAME", length = 15)
    private String tmmName;

    @Size(max = 50)
    @Column(name = "TMM_NIKNAME", length = 50)
    private String tmmNikname;

    @Size(max = 100)
    @NotNull
    @Column(name = "TMM_EMAIL", nullable = false, length = 100)
    private String tmmEmail;

    @Size(max = 255)
    @Column(name = "TMM_TOKEN")
    private String tmmToken;

    @Column(name = "TMM_SNS_TYPE", length = Integer.MAX_VALUE)
    private String tmmSnsType;

    @NotNull
    @ColumnDefault("'N'::bpchar")
    @Column(name = "TMM_AUTH_YN", nullable = false, length = Integer.MAX_VALUE)
    private String tmmAuthYn;

    @NotNull
    @Column(name = "TMM_REG_DT", nullable = false)
    private Instant tmmRegDt;

    @Size(max = 255)
    @Column(name = "TMM_PROFILE_URL")
    private String tmmProfileUrl;

    @Size(max = 50)
    @NotNull
    @Column(name = "TMM_UUID", nullable = false, length = 50)
    private String tmmUuid;

    @NotNull
    @Column(name = "TMM_GENDER", nullable = false, length = Integer.MAX_VALUE)
    private String tmmGender;
}
