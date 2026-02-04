package com.spoons.popparazzi.seq.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "TBL_SEQ_MASTER")
@Getter
@Setter
public class Seq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String TSM_NAME;

    private int TSM_NO;

    private String TSM_DESC;
}
