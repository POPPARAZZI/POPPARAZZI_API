package com.spoons.popparazzi.file.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalFileStorageService implements FileStorageService {

    @Value("${file.local.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.local.public-prefix:/files}")
    private String publicPrefix;

    @Override
    public String save(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is empty");
        }

        String originalName = file.getOriginalFilename();
        String ext = StringUtils.getFilenameExtension(originalName);

        String filename = UUID.randomUUID() + (ext == null || ext.isBlank() ? "" : "." + ext);

        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return publicPrefix + "/" + filename;

        } catch (IOException e) {
            throw new IllegalStateException("failed to save file", e);
        }
    }
}