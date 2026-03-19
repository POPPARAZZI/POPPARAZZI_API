package com.spoons.popparazzi.auth.service;

import com.spoons.popparazzi.auth.command.MemberSignupCommand;
import com.spoons.popparazzi.auth.entity.Member;
import com.spoons.popparazzi.auth.entity.enums.SnsType;
import com.spoons.popparazzi.auth.repository.MemberJpaRepository;
import com.spoons.popparazzi.error.exception.BusinessException;
import com.spoons.popparazzi.seq.service.SeqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.spoons.popparazzi.auth.error.MemberErrorCode.ALREADY_SIGNUP;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{

    private final MemberJpaRepository memberJpaRepository;

    private final SeqService seqService;

    @Override
    public void signup(MemberSignupCommand memberCommand) {

        if(memberJpaRepository.existsByEmail(memberCommand.email())) {
            throw new BusinessException(ALREADY_SIGNUP);
        }

        Member member = new Member(memberCommand.email()
                , memberCommand.pwd()
                , memberCommand.email()
                , memberCommand.nickName()
                , memberCommand.gender()
                , SnsType.E
                , seqService.getUuid());

        seqService.getSeqCode(member);

        memberJpaRepository.save(member);

    }
}
