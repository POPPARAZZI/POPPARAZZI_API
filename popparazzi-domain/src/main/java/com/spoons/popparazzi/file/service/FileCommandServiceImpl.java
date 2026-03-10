package com.spoons.popparazzi.file.service;

import com.spoons.popparazzi.common.YesNo;
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

    private final FileMasterRepository fileMasterRepository;
    private final FileStorageService fileStorageService;

    /**
     * 최종 등록 시점에 파일을 업로드하고, 바로 부모코드(parentCode)에 연결해서 저장한다.
     * - files가 null/empty면 첨부 없음으로 보고 그냥 종료
     */
    @Override
    public void saveFiles(List<MultipartFile> files, FileType fileType, String parentCode) {

        if (files == null || files.isEmpty()) {
            return;
        }

        if (parentCode == null || parentCode.isBlank()) {
            throw new BusinessException(FileErrorCode.FILE_INVALID_PARENT);
        }

        if (fileType == null) {
            throw new BusinessException(FileErrorCode.FILE_INVALID_TYPE);
        }

        List<MultipartFile> filtered = files.stream()
                .filter(Objects::nonNull)
                .filter(f -> !f.isEmpty())
                .toList();

        if (filtered.isEmpty()) {
            return;
        }

        if (filtered.size() > MAX_FILES) {
            throw new BusinessException(FileErrorCode.FILE_TOO_MANY);
        }

        // 저장소 업로드 + DB insert (parentCode는 바로 실제 코드)
        for (MultipartFile file : filtered) {
            String url = fileStorageService.save(file);

            fileMasterRepository.save(
                    FileMaster.create(parentCode, url, fileType)
            );
        }
    }

    @Override
    public void deleteFiles(String parentCode, FileType fileType) {
        List<FileMaster> files = fileMasterRepository
                .findAllByParentCodeAndFmTypeAndDeleteYn(parentCode, fileType, YesNo.NO);

        if (files.isEmpty()) {
            return;
        }

        for (FileMaster file : files) {
            fileStorageService.delete(file.getUrl());
            file.softDelete();
        }
    }

    /* 수정용 삭제 */
    @Override
    public void deleteFilesExceptKeep(String parentCode, FileType fileType, List<Long> keepFileSeqs) {

        List<Long> keepSeqs = keepFileSeqs == null ? List.of() : keepFileSeqs;

        List<FileMaster> files = fileMasterRepository.findAllByParentCodeAndFmTypeAndDeleteYn(
                parentCode,
                fileType,
                YesNo.NO
        );

        for (FileMaster file : files) {
            if (keepSeqs.contains(file.getFmSeq())) {
                continue;
            }

            fileStorageService.delete(file.getUrl());
            file.softDelete();
        }
    }
}