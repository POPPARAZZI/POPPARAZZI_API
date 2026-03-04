package com.spoons.popparazzi.file.service;

import com.spoons.popparazzi.error.exception.BusinessException;
import com.spoons.popparazzi.file.entity.FileMaster;
import com.spoons.popparazzi.file.enums.FileType;
import com.spoons.popparazzi.file.error.FileErrorCode;
import com.spoons.popparazzi.file.repository.FileMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class FileCommandServiceImpl implements FileCommandService {

    private static final int MAX_FILES = 5;
    private static final String TEMP_PARENT_CODE_MOIM = "MOIM_TEMP";

    private final FileMasterRepository fileMasterRepository;
    private final FileStorageService fileStorageService;

    @Override
    public List<Long> uploadTemp(List<MultipartFile> files, FileType fileType) {

        if (files == null || files.isEmpty()) {
            throw new BusinessException(FileErrorCode.FILE_EMPTY);
        }

        List<MultipartFile> filtered = files.stream()
                .filter(Objects::nonNull)
                .filter(f -> !f.isEmpty())
                .toList();

        if (filtered.isEmpty()) {
            throw new BusinessException(FileErrorCode.FILE_EMPTY);
        }

        if (filtered.size() > MAX_FILES) {
            throw new BusinessException(FileErrorCode.FILE_TOO_MANY);
        }

        String tempParent = resolveTempParentCode(fileType);

        return filtered.stream()
                .map(file -> {
                    String url = fileStorageService.save(file);
                    FileMaster saved = fileMasterRepository.save(
                            FileMaster.create(tempParent, url, fileType)
                    );
                    return saved.getFmSeq();
                })
                .toList();
    }

    @Override
    public void attachToParent(List<Long> fileSeqs, String parentCode, FileType fileType) {

        if (fileSeqs == null || fileSeqs.isEmpty()) {
            return;
        }

        if (parentCode == null || parentCode.isBlank()) {
            throw new BusinessException(FileErrorCode.FILE_INVALID_PARENT);
        }

        List<Long> ids = fileSeqs.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (ids.size() > MAX_FILES) {
            throw new BusinessException(FileErrorCode.FILE_TOO_MANY);
        }

        String tempParent = resolveTempParentCode(fileType);

        int updated = fileMasterRepository.attachToParentFromTemp(ids, parentCode, tempParent);

        if (updated != ids.size()) {
            throw new BusinessException(FileErrorCode.FILE_ATTACH_FAILED);
        }
    }

    private String resolveTempParentCode(FileType fileType) {
        if (fileType == FileType.M) {
            return TEMP_PARENT_CODE_MOIM;
        }
        return "TEMP";
    }
}