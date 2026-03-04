package com.spoons.popparazzi.seq.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tbl_seq_master")
@Getter
@Setter
public class Seq {

    @Id
    @Column(name="tsm_name", length = 6, nullable = false)
    private String tsmName;

    private int tsmNo;

    private String tsmDesc;
}
