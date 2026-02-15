package com.spoons.popparazzi.seq.service;

import com.spoons.popparazzi.moim.entity.Moim;
import com.spoons.popparazzi.seq.repository.SeqRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            ((Moim) vo).setMoimCode(code);
        }

        return vo;
    }
}
