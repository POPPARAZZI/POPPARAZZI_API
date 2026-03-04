package com.spoons.popparazzi.file.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * 파일을 저장하고, 접근 가능한 URL(또는 경로)을 반환한다.
     */
    String save(MultipartFile file);
}
