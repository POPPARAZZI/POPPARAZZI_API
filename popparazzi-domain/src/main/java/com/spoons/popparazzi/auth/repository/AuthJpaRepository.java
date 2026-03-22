package com.spoons.popparazzi.auth.repository;

import com.spoons.popparazzi.auth.entity.Member;
import com.spoons.popparazzi.auth.entity.enums.SnsType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthJpaRepository extends JpaRepository<Member, String> {

    boolean existsByEmail(String email);

    Optional<Member> findByMemberId(String memberId );
    Optional<Member> findByMemberUuid(String memberUuid);

    Optional<Member> findByMemberIdAndSnsType(String memberId, SnsType snsType);

    Member findByToken(String token);
}
