package com.spoons.popparazzi.file.service;

import com.spoons.popparazzi.file.enums.FileType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileCommandService {

    /**
     * 임시 parentCode로 업로드(저장 + DB insert) 후 fileSeq 리스트 반환
     */
    List<Long> uploadTemp(List<MultipartFile> files, FileType fileType);

    /**
     * 임시(TEMP)로 올라간 파일들을 실제 parentCode로 연결
     */
    void attachToParent(List<Long> fileSeqs, String parentCode, FileType fileType);
}