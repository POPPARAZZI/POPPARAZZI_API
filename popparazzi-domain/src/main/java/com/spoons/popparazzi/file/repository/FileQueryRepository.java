package com.spoons.popparazzi.file.repository;

import com.spoons.popparazzi.file.dto.query.FileDetailQuery;
import com.spoons.popparazzi.file.enums.FileType;

import java.util.List;

public interface FileQueryRepository {

    List<FileDetailQuery> findDetails(FileType type, String parentCode);
}