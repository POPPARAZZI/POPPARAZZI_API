package com.spoons.popparazzi.file.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spoons.popparazzi.file.dto.query.FileThumbQuery;
import com.spoons.popparazzi.file.entity.FileType;
import com.spoons.popparazzi.file.entity.QFileMaster;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.spoons.popparazzi.file.entity.QFileMaster.fileMaster;

@Repository
@RequiredArgsConstructor
public class FileThumbQueryRepositoryImpl implements FileThumbQueryRepository {

    private final JPAQueryFactory queryFactory;

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
}
