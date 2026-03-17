package com.spoons.popparazzi.popup.entity;

import com.spoons.popparazzi.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_popup_view_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopupViewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pvh_seq", nullable = false)
    private Long seq;

    @Column(name = "pvh_pm_code", length = 22, nullable = false)
    private String popupCode;

    @Column(name = "pvh_tmm_code", length = 22)
    private String memberCode;

    @CreationTimestamp
    @Column(name = "pvh_view_dt", nullable = false, updatable = false)
    private LocalDateTime viewDt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "pvh_pm_code",
            referencedColumnName = "pm_code",
            insertable = false,
            updatable = false
    )
    private Popup popup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "pvh_tmm_code",
            referencedColumnName = "tmm_code",
            insertable = false,
            updatable = false
    )
    private Member member;

    public static PopupViewHistory create(String popupCode, String memberCode) {
        PopupViewHistory history = new PopupViewHistory();
        history.popupCode = popupCode;
        history.memberCode = memberCode;
        return history;
    }
}