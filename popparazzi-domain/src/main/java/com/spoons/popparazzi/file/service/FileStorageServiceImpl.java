package com.spoons.popparazzi.file.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path uploadDir = Paths.get("uploads");
    private final String publicPrefix = "/uploads";

    @Override
    public String save(MultipartFile file) {
        try {
            if (Files.notExists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path target = uploadDir.resolve(filename);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return publicPrefix + "/" + filename;

        } catch (IOException e) {
            throw new IllegalStateException("파일을 저장할 수 없습니다.", e);
        }
    }

    @Override
    public void delete(String url) {
        if (url == null || url.isBlank()) return;

        try {
            // url이 "/uploads/xxx.png" 라면 마지막 파일명만 뽑기
            String filename = extractFilename(url);
            if (filename.isBlank()) return;

            Path target = uploadDir.resolve(filename);

            // 파일이 없으면 그냥 false 반환 (예외 안 터짐)
            Files.deleteIfExists(target);

        } catch (IOException e) {
            throw new IllegalStateException("파일을 삭제할 수 없습니다.", e);
        }
    }

    private String extractFilename(String url) {
        // "/uploads/abc.png" or "uploads/abc.png" 다 대응
        int idx = url.lastIndexOf('/');
        if (idx == -1) return url.trim();
        return url.substring(idx + 1).trim();
    }
}