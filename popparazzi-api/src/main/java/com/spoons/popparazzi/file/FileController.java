package com.spoons.popparazzi.file;

import com.spoons.popparazzi.file.dto.response.FileUploadResponse;
import com.spoons.popparazzi.file.enums.FileType;
import com.spoons.popparazzi.file.service.FileCommandService;
import com.spoons.popparazzi.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final FileCommandService fileCommandService;

    /**
     * 임시 업로드
     * - FileMaster에 parentCode = MOIM_TEMP로 저장
     * - 응답으로 fmSeq 리스트 반환
     */
    @PostMapping(
            value = "/temp",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<FileUploadResponse> uploadTemp(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("type") FileType type
    ) {
        List<Long> fileSeqs = fileCommandService.uploadTemp(files, type);
        return ApiResponse.success(new FileUploadResponse(fileSeqs));
    }
}