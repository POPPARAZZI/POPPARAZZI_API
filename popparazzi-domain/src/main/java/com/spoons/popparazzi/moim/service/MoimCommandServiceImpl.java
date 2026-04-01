package com.spoons.popparazzi.moim.service;

import com.spoons.popparazzi.category.entity.CategoryMapping;
import com.spoons.popparazzi.category.entity.CategoryMaster;
import com.spoons.popparazzi.category.enums.CategoryType;
import com.spoons.popparazzi.category.repository.CategoryMappingRepository;
import com.spoons.popparazzi.category.repository.CategoryMasterRepository;
import com.spoons.popparazzi.common.YesNo;
import com.spoons.popparazzi.error.exception.BusinessException;
import com.spoons.popparazzi.file.enums.FileType;
import com.spoons.popparazzi.file.service.FileCommandService;
import com.spoons.popparazzi.moim.dto.command.CreateMoimCommand;
import com.spoons.popparazzi.moim.dto.command.UpdateMoimCommand;
import com.spoons.popparazzi.moim.entity.Moim;
import com.spoons.popparazzi.moim.entity.MoimMemberMapping;
import com.spoons.popparazzi.moim.error.MoimErrorCode;
import com.spoons.popparazzi.moim.repository.MoimMemberMappingRepository;
import com.spoons.popparazzi.moim.repository.MoimRepository;
import com.spoons.popparazzi.popup.repository.PopupRepository;
import com.spoons.popparazzi.seq.service.SeqService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MoimCommandServiceImpl implements MoimCommandService {

    private static final int MAX_PARTICIPANTS_MIN = 2;
    private static final int MAX_PARTICIPANTS_MAX = 9;

    private static final int CATEGORY_MIN = 1;
    private static final int CATEGORY_MAX = 3;

    private final MoimRepository moimRepository;
    private final MoimMemberMappingRepository moimMemberMappingRepository;

    private final PopupRepository popupRepository;

    private final CategoryMasterRepository categoryMasterRepository;
    private final CategoryMappingRepository categoryMappingRepository;

    private final SeqService seqService;

    private final FileCommandService fileCommandService;

    /* 1. 모임 생성 */
    @Override
    public String create(CreateMoimCommand command, List<MultipartFile> files, String leaderMemberCode) {

        // 0) 기본 방어
        if (command == null) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }
        if (leaderMemberCode == null || leaderMemberCode.isBlank()) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }

        // 1) 팝업 존재 검증
        String popupCode = command.getPopupCode();

        if (popupCode == null || popupCode.isBlank()) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }

        boolean popupExists = popupRepository.existsById(popupCode);

        if (!popupExists) {
            throw new BusinessException(MoimErrorCode.MOIM_NOT_FOUND); // 원하면 INVALID_REQUEST로 바꿔도 됨
        }

        // 2) 정원 검증 (1~9)
        int maxParticipants = command.getMaxParticipants();

        if (maxParticipants < MAX_PARTICIPANTS_MIN || maxParticipants > MAX_PARTICIPANTS_MAX) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }

        // 3) 일정 검증 (과거 불가)
        LocalDateTime scheduleAt = command.getScheduleAt();

        if (scheduleAt == null || scheduleAt.isBefore(LocalDateTime.now())) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }

        // 4) 카테고리 검증 (1~3, 중복 제거, 존재 + type=M)
        List<String> categoryCodes = validateMoimCategoryCodes(command.getCategoryCodes());

        // 5) Moim 생성 + 코드 세팅
        Moim moim = Moim.create(
                popupCode,
                leaderMemberCode,
                scheduleAt,
                maxParticipants,
                requireText(command.getTitle()),
                requireText(command.getContent()),
                command.getPreQuestion() == null ? "" : command.getPreQuestion()
        );

        seqService.getSeqCode(moim);

        // 6) 모임 저장
        Moim saved = moimRepository.save(moim);
        String moimCode = saved.getMoimCode();

        // 7) 방장 자동 승인 매핑 저장
        MoimMemberMapping leaderMapping = MoimMemberMapping.leader(moimCode, leaderMemberCode);
        moimMemberMappingRepository.save(leaderMapping);

        // 8) 카테고리 매핑 저장
        List<CategoryMapping> mappings = categoryCodes.stream()
                .map(code -> CategoryMapping.of(code, moimCode))
                .collect(Collectors.toList());

        categoryMappingRepository.saveAll(mappings);

        // 9) 파일 저장 (전략 C)
        fileCommandService.saveFiles(files, FileType.M, moimCode);

        return moimCode;
    }

    private String requireText(String value) {
        if (value == null) return "";
        return value.trim();
    }

    /* 2. 모임 수정 */
    @Override
    public String update(UpdateMoimCommand command, List<MultipartFile> files, String requesterMemberCode) {

        // 0) 기본 방어
        if (command == null) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }

        String moimCode = command.getMoimCode();

        if (moimCode == null || moimCode.isBlank()) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }

        // 1) 수정 대상 모임 조회
        Moim moim = moimRepository.findByMoimCodeAndDeleteYn(moimCode, YesNo.NO)
                .orElseThrow(() -> new BusinessException(MoimErrorCode.MOIM_NOT_FOUND));

        // 2) 작성자 권한 체크
        validateUpdateAuthority(moim, requesterMemberCode);

        // 3) 정원 검증
        int maxParticipants = command.getMaxParticipants();

        if (maxParticipants < MAX_PARTICIPANTS_MIN || maxParticipants > MAX_PARTICIPANTS_MAX) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }

        long currentParticipants = moimMemberMappingRepository.countByIdMoimCodeAndIsApprovedTrueAndJoinYn(moimCode, YesNo.YES);

        if (maxParticipants < currentParticipants) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }

        // 4) 카테고리 검증
        List<String> categoryCodes = validateMoimCategoryCodes(command.getCategoryCodes());

        // 5) 모임 기본 정보 수정
        moim.setMaxParticipants(maxParticipants);
        moim.setTitle(requireText(command.getTitle()));
        moim.setBody(requireText(command.getContent()));
        moim.setPreQuestion(command.getPreQuestion() == null ? "" : command.getPreQuestion().trim());

        // 6) 카테고리 전체 교체
        replaceCategories(moimCode, categoryCodes);

        // 7) 기존 파일 중 keepFileSeqs에 없는 것 삭제
        List<Long> keepFileSeqs = command.getKeepFileSeqs() == null ? List.of() : command.getKeepFileSeqs();
        fileCommandService.deleteFilesExceptKeep(moimCode, FileType.M, keepFileSeqs);

        // 8) 새 파일 추가 저장
        fileCommandService.saveFiles(files, FileType.M, moimCode);

        return moimCode;
    }

    private void validateUpdateAuthority(Moim moim, String requesterMemberCode) {
        if (!moim.getLeaderMemberCode().equals(requesterMemberCode)) {
            throw new BusinessException(MoimErrorCode.MOIM_UPDATE_FORBIDDEN);
        }
    }

    private void replaceCategories(String moimCode, List<String> categoryCodes) {
        categoryMappingRepository.deleteByParentCode(moimCode);

        List<CategoryMapping> mappings = categoryCodes.stream()
                .map(code -> CategoryMapping.of(code, moimCode))
                .collect(Collectors.toList());

        categoryMappingRepository.saveAll(mappings);
    }

    /* 3. 모임 삭제
    * 모임 존재 확인, 작성자 확인, 모임 당일 삭제 불가
    * 첨부파일 실제 스토리지 삭제, file_master : soft delete, moim : soft delete */
    @Override
    public void delete(String moimCode, String requesterMemberCode) {
        Moim moim = moimRepository.findByMoimCodeAndDeleteYn(moimCode, YesNo.NO)
                .orElseThrow(() -> new BusinessException(MoimErrorCode.MOIM_NOT_FOUND));

        validateDeleteAuthority(moim, requesterMemberCode);
        validateDeleteDate(moim);

        fileCommandService.deleteFiles(moimCode, FileType.M);

        moim.softDelete();
    }

    /* 공통 검증 */
    // 삭제 권한 : 작성자만
    private void validateDeleteAuthority(Moim moim, String requesterMemberCode) {
        if (!moim.getLeaderMemberCode().equals(requesterMemberCode)) {
            throw new BusinessException(MoimErrorCode.MOIM_DELETE_FORBIDDEN);
        }
    }

    // 삭제 조건 : 당일 불가
    private void validateDeleteDate(Moim moim) {
        LocalDate today = LocalDate.now();

        if (moim.getDate().toLocalDate().isEqual(today)) {
            throw new BusinessException(MoimErrorCode.MOIM_DELETE_NOT_ALLOWED_ON_EVENT_DAY);
        }
    }

    /* 카테고리 검증
    * null 방어, trim, blank 제거, distinct, 1-3개 개수 체크, DB 존재 여부, 타입 체크*/
    private List<String> validateMoimCategoryCodes(List<String> rawCategoryCodes) {

        List<String> categoryCodes = rawCategoryCodes == null ? List.of() : rawCategoryCodes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

        if (categoryCodes.size() < CATEGORY_MIN || categoryCodes.size() > CATEGORY_MAX) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }

        List<CategoryMaster> categories = categoryMasterRepository.findAllByCodeIn(categoryCodes);

        if (categories.size() != categoryCodes.size()) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }

        boolean hasInvalidType = categories.stream()
                .anyMatch(c -> c.getType() != CategoryType.M);

        if (hasInvalidType) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }

        return categoryCodes;
    }



}