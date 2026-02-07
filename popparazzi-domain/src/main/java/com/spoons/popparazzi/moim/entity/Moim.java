package com.spoons.popparazzi.moim.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

/*
* DB랑 연결하는 엔티티
* */
@Entity
@Table(name = "TBL_MOIM_MASTER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
public class Moim {

    @Id
    @Column(name = "MM_CODE", length = 22, nullable = false)
    private String MM_CODE;

}
