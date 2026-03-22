package com.spoons.popparazzi.seq.service;

import com.spoons.popparazzi.auth.entity.Member;
import com.spoons.popparazzi.moim.entity.Moim;
import com.spoons.popparazzi.seq.entity.Seq;
import com.spoons.popparazzi.seq.repository.SeqRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class SeqServiceImpl implements SeqService{

    private final SeqRepository seqRepository;

    @Override
    @Transactional
    public Object getSeqCode(Object vo) {

        if (vo instanceof Moim) {
            String code = seqRepository.getUniqueCode("MOIM", "MM");
            ((Moim) vo).setMM_CODE(code);
        } else if (vo instanceof Member) {
            String code = seqRepository.getUniqueCode("MEMBER", "TMM");
            ((Member) vo).setMemberCode(code);
        }

        return vo;
    }

    @Override
    public UUID getUuid() {
        // TODO Auto-generated method stub

        return UUID.randomUUID();
    }
}
