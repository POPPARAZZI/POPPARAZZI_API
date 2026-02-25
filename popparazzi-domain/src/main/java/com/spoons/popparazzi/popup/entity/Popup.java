package com.spoons.popparazzi.popup.entity;

import com.spoons.popparazzi.common.YesNo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_popup_master")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
public class Popup {

    @Id
    @Column(name = "pm_code", length = 22, nullable = false)
    private String popupCode;

    @Column(name = "pm_title", nullable = false)
    private String title;

    @Column(name = "pm_start_dt", nullable = false)
    private LocalDateTime startDt;

    @Column(name = "pm_end_dt", nullable = false)
    private LocalDateTime endDt;

    @Column(name = "pm_hours", length = 100, nullable = false)
    private String hours;

    @Column(name = "pm_zipcode", length = 10, nullable = false)
    private String zipcode;

    @Column(name = "pm_addr_sido", length = 10, nullable = false)
    private String addrSido;

    @Column(name = "pm_addr_sigungu", length = 10, nullable = false)
    private String addrSigungu;

    @Column(name = "pm_addr_dong", length = 10)
    private String addrDong;

    @Column(name = "pm_addr_road_name", length = 50)
    private String addrRoadName;

    @Column(name = "pm_addr_detail", length = 100)
    private String addrDetail;

    @Column(name = "pm_benefit", length = 255)
    private String benefit;

    @Column(name = "pm_body", columnDefinition = "text", nullable = false)
    private String body;

    @Column(name = "pm_url", length = 255)
    private String url;

    @Column(name = "pm_pre_reserve_yn", length = 1, nullable = false)
    private YesNo preReserveYn;

    @Column(name = "pm_reg_dt", nullable = false)
    private LocalDateTime regDt;
}