package com.spoons.popparazzi.moim.service;

import com.spoons.popparazzi.category.entity.CategoryMapping;
import com.spoons.popparazzi.category.entity.CategoryMaster;
import com.spoons.popparazzi.category.enums.CategoryType;
import com.spoons.popparazzi.category.repository.CategoryMappingRepository;
import com.spoons.popparazzi.category.repository.CategoryMasterRepository;
import com.spoons.popparazzi.error.exception.BusinessException;
import com.spoons.popparazzi.file.enums.FileType;
import com.spoons.popparazzi.file.service.FileCommandService;
import com.spoons.popparazzi.moim.dto.command.CreateMoimCommand;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MoimCommandServiceImpl implements MoimCommandService {

    private static final int MAX_PARTICIPANTS_MIN = 1;
    private static final int MAX_PARTICIPANTS_MAX = 9;

    private static final int CATEGORY_MIN = 1;
    private static final int CATEGORY_MAX = 3;

    private final MoimRepository moimRepository;
    private final MoimMemberMappingRepository moimMemberMappingRepository;

    private final PopupRepository popupRepository;

    private final CategoryMasterRepository categoryMasterRepository;
    private final CategoryMappingRepository categoryMappingRepository;

    private final SeqService seqService;

    // ✅ 파일 선업로드(임시) -> 모임 생성 후 parentCode 연결
    private final FileCommandService fileCommandService;

    // 1. 모임 생성
    @Override
    public String create(CreateMoimCommand command, String leaderMemberCode) {

        // 0) 기본 방어
        if (command == null) {
            throw new BusinessException(MoimErrorCode.MOIM_NOT_FOUND);
        }
        if (leaderMemberCode == null || leaderMemberCode.isBlank()) {
            throw new BusinessException(MoimErrorCode.MOIM_NOT_FOUND);
        }

        // 1) 팝업 존재 검증
        String popupCode = command.getPopupCode();
        if (popupCode == null || popupCode.isBlank()) {
            throw new BusinessException(MoimErrorCode.MOIM_NOT_FOUND);
        }
        if (!popupRepository.existsById(popupCode)) {
            throw new BusinessException(MoimErrorCode.MOIM_NOT_FOUND);
        }

        // 2) 정원 검증 (1~9)
        int maxParticipants = command.getMaxParticipants();
        if (maxParticipants < MAX_PARTICIPANTS_MIN || maxParticipants > MAX_PARTICIPANTS_MAX) {
            throw new BusinessException(MoimErrorCode.MOIM_NOT_FOUND);
        }

        // 3) 일정 검증 (과거 불가)
        LocalDateTime scheduleAt = command.getScheduleAt();
        if (scheduleAt == null || scheduleAt.isBefore(LocalDateTime.now())) {
            throw new BusinessException(MoimErrorCode.MOIM_NOT_FOUND);
        }

        // 4) 카테고리 검증 (1~3, 중복 제거, 존재 + type=M)
        List<String> rawCategoryCodes =
                command.getCategoryCodes() == null ? List.of() : command.getCategoryCodes();

        List<String> categoryCodes = rawCategoryCodes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

        if (categoryCodes.size() < CATEGORY_MIN || categoryCodes.size() > CATEGORY_MAX) {
            throw new BusinessException(MoimErrorCode.MOIM_NOT_FOUND);
        }

        List<CategoryMaster> categories = categoryMasterRepository.findAllByCodeIn(categoryCodes);

        if (categories.size() != categoryCodes.size()) {
            throw new BusinessException(MoimErrorCode.MOIM_NOT_FOUND);
        }

        boolean hasInvalidType = categories.stream()
                .anyMatch(c -> c.getType() != CategoryType.M);

        if (hasInvalidType) {
            throw new BusinessException(MoimErrorCode.MOIM_NOT_FOUND);
        }

        // 5) Moim 생성 + 코드 세팅 (Moim.create 팩토리 사용)
        Moim moim = Moim.create(
                popupCode,
                leaderMemberCode,
                scheduleAt,
                maxParticipants,
                requireText(command.getTitle()),
                requireText(command.getContent()),
                command.getPreQuestion() == null ? "" : command.getPreQuestion()
        );

        // mm_code 생성
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

        // ✅ 9) 파일 연결(전략 B)
        // - 파일은 이미 /files/temp 로 업로드되어 parentCode=MOIM_TEMP 상태
        // - 여기서 parentCode를 실제 moimCode로 업데이트
        fileCommandService.attachToParent(command.getFileSeqs(), moimCode, FileType.M);

        return moimCode;
    }

    private String requireText(String value) {
        if (value == null) return "";
        return value.trim();
    }
}