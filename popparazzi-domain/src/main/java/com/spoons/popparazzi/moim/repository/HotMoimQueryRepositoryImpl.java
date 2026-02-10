package com.spoons.popparazzi.moim.repository;

import com.spoons.popparazzi.file.entity.FileType;
import com.spoons.popparazzi.like.entity.LikeType;
import com.spoons.popparazzi.moim.dto.query.FileThumbQuery;
import com.spoons.popparazzi.moim.dto.query.HotMoimCardQuery;
import com.spoons.popparazzi.moim.dto.query.HotMoimRankQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class HotMoimQueryRepositoryImpl implements HotMoimQueryRepository {

    @PersistenceContext
    private EntityManager em;

    // 랭킹 조회
    @Override
    public List<HotMoimRankQuery> findHotRankKeys(LikeType type, LocalDateTime since, Pageable pageable) {
        // LikeMapping.type = :type AND createdAt >= :since
        // group by targetCode (모임코드) order by count desc
        return em.createQuery("""
            select new com.spoons.popparazzi.moim.dto.query.HotMoimRankQuery(
                l.targetCode,
                count(l)
            )
            from LikeMapping l
            where l.type = :type
              and l.createdAt >= :since
            group by l.targetCode
            order by count(l) desc
        """, HotMoimRankQuery.class)
                .setParameter("type", type)
                .setParameter("since", since)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    // 썸네일 찾기
    @Override
    public List<FileThumbQuery> findFirstThumbs(FileType type, List<String> parentCodes) {
        if (parentCodes == null || parentCodes.isEmpty()) return List.of();

        return em.createQuery("""
        select new com.spoons.popparazzi.moim.dto.query.FileThumbQuery(
            f.parentCode,
            f.url
        )
        from FileMaster f
        where f.type = :type
          and f.parentCode in :codes
          and f.fmSeq = (
              select min(f2.fmSeq)
              from FileMaster f2
              where f2.type = f.type
                and f2.parentCode = f.parentCode
          )
    """, FileThumbQuery.class)
                .setParameter("type", type)
                .setParameter("codes", parentCodes)
                .getResultList();
    }

    // 핫한 모임 카드 조립
    @Override
    public List<HotMoimCardQuery> findHotCardsBase(List<String> mmCodes) {
        if (mmCodes == null || mmCodes.isEmpty()) return List.of();

        return em.createQuery("""
        select new com.spoons.popparazzi.moim.dto.query.HotMoimCardQuery(
            m.mmCode,
            m.mmPmCode,
            m.mmTitle,
            m.mmDate,
            cast(count(case when mp.joinYn = 'Y' then 1 else null end) as int),
            m.mmMaxParticipants,
            null,
            0
        )
        from Moim m
        left join MoimMemberMapping mp
               on mp.id.mmCode = m.mmCode
        where m.mmCode in :codes
          and m.mmDeleteYn = 'N'
        group by m.mmCode, m.mmPmCode, m.mmTitle, m.mmDate, m.mmMaxParticipants
    """, HotMoimCardQuery.class)
                .setParameter("codes", mmCodes)
                .getResultList();
    }

}
