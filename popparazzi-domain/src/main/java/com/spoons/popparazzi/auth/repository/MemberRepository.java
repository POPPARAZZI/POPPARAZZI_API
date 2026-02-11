package com.spoons.popparazzi.auth.repository;

import com.spoons.popparazzi.auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, String> {
}
