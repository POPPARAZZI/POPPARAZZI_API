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
import com.spoons.popparazzi.moim.enums.MoimMemberStatus;
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

    // ─────────────────────────────────────────────────────────────────────────
    // 모임 생성
    // ─────────────────────────────────────────────────────────────────────────

    public String create(CreateMoimCommand command, List<MultipartFile> files, String leaderMemberCode) {
        validateCreateRequest(command, leaderMemberCode);

        String popupCode = command.getPopupCode();
        if (!popupRepository.existsById(popupCode)) {
            throw new BusinessException(MoimErrorCode.MOIM_NOT_FOUND);
        }

        validateMaxParticipants(command.getMaxParticipants());
        validateScheduleAt(command.getScheduleAt());

        List<String> categoryCodes = validateCategoryCodes(command.getCategoryCodes());

        Moim moim = Moim.create(
                popupCode,
                leaderMemberCode,
                command.getScheduleAt(),
                command.getMaxParticipants(),
                trimOrEmpty(command.getTitle()),
                trimOrEmpty(command.getContent()),
                trimOrEmpty(command.getPreQuestion())
        );

        seqService.getSeqCode(moim);

        Moim saved = moimRepository.save(moim);
        String moimCode = saved.getMoimCode();

        moimMemberMappingRepository.save(MoimMemberMapping.leader(moimCode, leaderMemberCode));

        categoryMappingRepository.saveAll(
                categoryCodes.stream()
                        .map(code -> CategoryMapping.of(code, moimCode))
                        .collect(Collectors.toList())
        );

        fileCommandService.saveFiles(files, FileType.M, moimCode);

        return moimCode;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 모임 수정
    // ─────────────────────────────────────────────────────────────────────────

    public String update(UpdateMoimCommand command, List<MultipartFile> files, String requesterMemberCode) {
        if (command == null || command.getMoimCode() == null || command.getMoimCode().isBlank()) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }

        Moim moim = moimRepository.findByMoimCodeAndDeleteYn(command.getMoimCode(), YesNo.NO)
                .orElseThrow(() -> new BusinessException(MoimErrorCode.MOIM_NOT_FOUND));

        validateLeader(moim, requesterMemberCode, MoimErrorCode.MOIM_UPDATE_FORBIDDEN);
        validateMaxParticipants(command.getMaxParticipants());

        long currentParticipants = moimMemberMappingRepository
                .countByIdMoimCodeAndStatus(command.getMoimCode(), MoimMemberStatus.APPROVED);

        if (command.getMaxParticipants() < currentParticipants) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }

        List<String> categoryCodes = validateCategoryCodes(command.getCategoryCodes());

        moim.setMaxParticipants(command.getMaxParticipants());
        moim.setTitle(trimOrEmpty(command.getTitle()));
        moim.setBody(trimOrEmpty(command.getContent()));
        moim.setPreQuestion(trimOrEmpty(command.getPreQuestion()));

        replaceCategories(command.getMoimCode(), categoryCodes);

        List<Long> keepFileSeqs = command.getKeepFileSeqs() == null ? List.of() : command.getKeepFileSeqs();
        fileCommandService.deleteFilesExceptKeep(command.getMoimCode(), FileType.M, keepFileSeqs);
        fileCommandService.saveFiles(files, FileType.M, command.getMoimCode());

        return command.getMoimCode();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 모임 삭제
    // ─────────────────────────────────────────────────────────────────────────

    public void delete(String moimCode, String requesterMemberCode) {
        Moim moim = moimRepository.findByMoimCodeAndDeleteYn(moimCode, YesNo.NO)
                .orElseThrow(() -> new BusinessException(MoimErrorCode.MOIM_NOT_FOUND));

        validateLeader(moim, requesterMemberCode, MoimErrorCode.MOIM_DELETE_FORBIDDEN);
        validateDeleteDate(moim);

        fileCommandService.deleteFiles(moimCode, FileType.M);
        moim.softDelete();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 내부 검증
    // ─────────────────────────────────────────────────────────────────────────

    private void validateCreateRequest(CreateMoimCommand command, String leaderMemberCode) {
        if (command == null
                || leaderMemberCode == null || leaderMemberCode.isBlank()
                || command.getPopupCode() == null || command.getPopupCode().isBlank()) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }
    }

    private void validateMaxParticipants(int maxParticipants) {
        if (maxParticipants < MAX_PARTICIPANTS_MIN || maxParticipants > MAX_PARTICIPANTS_MAX) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }
    }

    private void validateScheduleAt(LocalDateTime scheduleAt) {
        if (scheduleAt == null || scheduleAt.isBefore(LocalDateTime.now())) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }
    }

    private void validateLeader(Moim moim, String requesterMemberCode, MoimErrorCode errorCode) {
        if (!moim.getLeaderMemberCode().equals(requesterMemberCode)) {
            throw new BusinessException(errorCode);
        }
    }

    private void validateDeleteDate(Moim moim) {
        if (moim.getDate().toLocalDate().isEqual(LocalDate.now())) {
            throw new BusinessException(MoimErrorCode.MOIM_DELETE_NOT_ALLOWED_ON_EVENT_DAY);
        }
    }

    private List<String> validateCategoryCodes(List<String> rawCategoryCodes) {
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

        if (categories.stream().anyMatch(c -> c.getType() != CategoryType.M)) {
            throw new BusinessException(MoimErrorCode.INVALID_REQUEST);
        }

        return categoryCodes;
    }

    private void replaceCategories(String moimCode, List<String> categoryCodes) {
        categoryMappingRepository.deleteByParentCode(moimCode);
        categoryMappingRepository.saveAll(
                categoryCodes.stream()
                        .map(code -> CategoryMapping.of(code, moimCode))
                        .collect(Collectors.toList())
        );
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
