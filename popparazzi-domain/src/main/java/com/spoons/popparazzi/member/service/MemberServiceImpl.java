package com.spoons.popparazzi.member.service;

import com.spoons.popparazzi.auth.command.MemberSignupCommand;
import com.spoons.popparazzi.auth.entity.Member;
import com.spoons.popparazzi.auth.entity.enums.SnsType;
import com.spoons.popparazzi.auth.repository.AuthJpaRepository;
import com.spoons.popparazzi.error.exception.BusinessException;
import com.spoons.popparazzi.seq.service.SeqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.spoons.popparazzi.auth.error.AuthErrorCode.ALREADY_SIGNUP;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{

}
