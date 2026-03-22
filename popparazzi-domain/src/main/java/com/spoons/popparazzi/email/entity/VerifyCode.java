package com.spoons.popparazzi.email.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Getter
@Entity
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_verify_code")
@EntityListeners(AuditingEntityListener.class)
public class VerifyCode {

    @Id
    @Column(name = "code")
    String code;

    @Column(name = "email", unique = true)
    String email;

    @Column(name="issue_date")
    @CreatedDate
    Date issueDate;

  /*  public VerifyCode(MemberIdCheckRequest dto){
        this.email = dto.email();
    }*/
}
