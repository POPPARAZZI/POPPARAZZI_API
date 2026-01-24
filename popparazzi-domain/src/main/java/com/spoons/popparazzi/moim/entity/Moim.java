package com.spoons.popparazzi.moim.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
* DB랑 연결하는 엔티티
* */
@Entity
@Table(name = "TBL_MOIM_MASTER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Moim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String MM_CODE;

    private String title;
}
