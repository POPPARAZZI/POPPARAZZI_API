package com.spoons.popparazzi.category.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spoons.popparazzi.category.dto.query.MoimCategoryRow;
import com.spoons.popparazzi.category.entity.QCategoryMapping;
import com.spoons.popparazzi.category.entity.QCategoryMaster;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CategoryQueryRepositoryImpl implements CategoryQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MoimCategoryRow> findMoimCategories(List<String> parentCodes) {

        if (parentCodes == null || parentCodes.isEmpty()) return List.of();

        QCategoryMapping mapping = QCategoryMapping.categoryMapping;
        QCategoryMaster master = QCategoryMaster.categoryMaster;

        return queryFactory
                .select(Projections.constructor(
                        MoimCategoryRow.class,
                        mapping.parentCode,
                        master.name
                ))
                .from(mapping)
                .join(master)
                .on(mapping.categoryCode.eq(master.code))
                .where(
                        mapping.parentCode.in(parentCodes),
                        master.type.eq("M")
                )
                .fetch();
    }
}

