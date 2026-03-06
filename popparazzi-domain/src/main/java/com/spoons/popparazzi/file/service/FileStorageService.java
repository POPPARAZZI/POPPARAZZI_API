package com.spoons.popparazzi.file.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * 파일 저장
     * @param file 업로드 파일
     * @return 저장된 파일 URL
     */
    String save(MultipartFile file);

    /**
     * 파일 삭제
     * @param url 저장된 파일 URL
     */
    void delete(String url);
}