package com.spoons.popparazzi.file.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spoons.popparazzi.common.YesNo;
import com.spoons.popparazzi.file.dto.query.FileDetailQuery;
import com.spoons.popparazzi.file.enums.FileType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.spoons.popparazzi.file.entity.QFileMaster.fileMaster;

@Repository
@RequiredArgsConstructor
public class FileQueryRepositoryImpl implements FileQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<FileDetailQuery> findDetails(FileType type, String parentCode) {
        if (type == null || parentCode == null || parentCode.isBlank()) {
            return List.of();
        }

        return queryFactory
                .select(Projections.constructor(
                        FileDetailQuery.class,
                        fileMaster.fmSeq,
                        fileMaster.url
                ))
                .from(fileMaster)
                .where(
                        fileMaster.fmType.eq(type),
                        fileMaster.parentCode.eq(parentCode),
                        fileMaster.deleteYn.eq(YesNo.NO)
                )
                .orderBy(fileMaster.fmSeq.asc())
                .fetch();
    }
}