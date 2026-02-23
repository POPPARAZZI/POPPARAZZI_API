package com.spoons.popparazzi.file.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spoons.popparazzi.file.dto.query.FileThumbQuery;
import com.spoons.popparazzi.file.enums.FileType;
import com.spoons.popparazzi.file.entity.QFileMaster;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.spoons.popparazzi.file.entity.QFileMaster.fileMaster;

@Repository
@RequiredArgsConstructor
public class FileThumbQueryRepositoryImpl implements FileThumbQueryRepository {

    private final JPAQueryFactory queryFactory;

    // 1. 모임 전용
    @Override
    public List<FileThumbQuery> findFirstThumbs(FileType type, List<String> parentCodes) {
        if (parentCodes == null || parentCodes.isEmpty()) return List.of();

        // 서브쿼리용 별칭(Q타입은 new로 하나 더 만들어야 함)
        var f2 = new QFileMaster("f2");

        return queryFactory
                .select(Projections.constructor(
                        FileThumbQuery.class,
                        fileMaster.parentCode,
                        fileMaster.url
                ))
                .from(fileMaster)
                .where(
                        fileMaster.fmType.eq(type),
                        fileMaster.parentCode.in(parentCodes),
                        fileMaster.fmSeq.eq(
                                JPAExpressions
                                        .select(f2.fmSeq.min())
                                        .from(f2)
                                        .where(
                                                f2.fmType.eq(fileMaster.fmType),
                                                f2.parentCode.eq(fileMaster.parentCode)
                                        )
                        )
                )
                .fetch();
    }

    /**
     * 2) 게시판 전용
     * - parentCode(=tbm_code) 별로 fm_seq가 가장 작은 1개만 썸네일로 선택
     * - 파일이 없는 parentCode는 결과에 포함되지 않음
     */
    @Override
    public List<FileThumbQuery> findFirstThumbsForPosts(List<String> parentCodes) {
        if (parentCodes == null || parentCodes.isEmpty()) {
            return List.of();
        }

        com.querydsl.jpa.JPAExpressions expressions = null;

        // 서브쿼리용 QFileMaster 별칭
        var fmSub = new com.spoons.popparazzi.file.entity.QFileMaster("fmSub");

        return queryFactory
                .select(Projections.constructor(
                        FileThumbQuery.class,
                        fileMaster.parentCode,
                        fileMaster.url
                ))
                .from(fileMaster)
                .where(
                        fileMaster.fmType.eq(FileType.R),
                        fileMaster.parentCode.in(parentCodes),
                        fileMaster.fmSeq.eq(
                                com.querydsl.jpa.JPAExpressions
                                        .select(fmSub.fmSeq.min())
                                        .from(fmSub)
                                        .where(
                                                fmSub.fmType.eq(FileType.R),
                                                fmSub.parentCode.eq(fileMaster.parentCode)
                                        )
                        )
                )
                .fetch();
    }
}
