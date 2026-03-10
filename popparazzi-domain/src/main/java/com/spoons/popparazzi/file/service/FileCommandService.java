package com.spoons.popparazzi.file.service;

import com.spoons.popparazzi.file.enums.FileType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileCommandService {

    /* 1. 파일 등록 */
    void saveFiles(List<MultipartFile> files, FileType fileType, String parentCode);

    /* 2. 파일 삭제 */
    void deleteFiles(String parentCode, FileType fileType);

    /* 3. 모임 수정시 남길 파일 제외 전부 삭제 */
    void deleteFilesExceptKeep(String parentCode, FileType fileType, List<Long> keepFileSeqs);
}