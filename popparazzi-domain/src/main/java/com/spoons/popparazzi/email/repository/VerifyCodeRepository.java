package com.spoons.popparazzi.email.repository;

import com.spoons.popparazzi.email.entity.VerifyCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerifyCodeRepository extends JpaRepository<VerifyCode, String> {

    VerifyCode findCodeByEmail(String email);

    void deleteByEmail(String email);

    VerifyCode findByCode(String code);

    void deleteByCode(String code);
}
