package com.spoons.popparazzi.file.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spoons.popparazzi.common.YesNo;
import com.spoons.popparazzi.file.dto.query.FileThumbQuery;
import com.spoons.popparazzi.file.entity.QFileMaster;
import com.spoons.popparazzi.file.enums.FileType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.spoons.popparazzi.file.entity.QFileMaster.fileMaster;

@Repository
@RequiredArgsConstructor
public class FileThumbQueryRepositoryImpl implements FileThumbQueryRepository {

    private final JPAQueryFactory queryFactory;

    // 1. 범용 썸네일
    @Override
    public List<FileThumbQuery> findFirstThumbs(FileType type, List<String> parentCodes) {
        if (parentCodes == null || parentCodes.isEmpty()) return List.of();

        var f2 = new QFileMaster("f2");

        return queryFactory
                .select(Projections.constructor(
                        FileThumbQuery.class,
                        fileMaster.parentCode,  // String parentCode
                        fileMaster.fmSeq,        // Long fileSeq
                        fileMaster.url           // String url
                ))
                .from(fileMaster)
                .where(
                        fileMaster.fmType.eq(type),
                        fileMaster.parentCode.in(parentCodes),
                        fileMaster.deleteYn.eq(YesNo.NO),
                        fileMaster.fmSeq.eq(
                                JPAExpressions
                                        .select(f2.fmSeq.min())
                                        .from(f2)
                                        .where(
                                                f2.fmType.eq(fileMaster.fmType),
                                                f2.parentCode.eq(fileMaster.parentCode),
                                                f2.deleteYn.eq(YesNo.NO) // ✅ 서브쿼리도 삭제 제외
                                        )
                        )
                )
                .fetch();
    }
}