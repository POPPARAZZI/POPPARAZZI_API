package com.spoons.popparazzi.moim.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DbProbeService {
    private final EntityManager em;

    public void logDbInfo() {
        var db = em.createNativeQuery("select current_database()").getSingleResult();
        var schema = em.createNativeQuery("select current_schema()").getSingleResult();
        var user = em.createNativeQuery("select current_user").getSingleResult();

        var moimCnt = em.createNativeQuery("select count(*) from \"TBL_MOIM_MASTER\"").getSingleResult();
        var likeCnt = em.createNativeQuery("select count(*) from \"TBL_LIKE_MAPPING\"").getSingleResult();

        log.info("DB_PROBE database={}, schema={}, user={}, moimCnt={}, likeCnt={}",
                db, schema, user, moimCnt, likeCnt);
    }
}
