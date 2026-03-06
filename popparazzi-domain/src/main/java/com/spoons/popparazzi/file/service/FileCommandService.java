package com.spoons.popparazzi.file.service;

import com.spoons.popparazzi.file.enums.FileType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileCommandService {

    /* 1. 파일 등록 */
    void saveFiles(List<MultipartFile> files, FileType fileType, String parentCode);

    /* 2. 파일 삭제 */
    void deleteFiles(String parentCode, FileType fileType);
}