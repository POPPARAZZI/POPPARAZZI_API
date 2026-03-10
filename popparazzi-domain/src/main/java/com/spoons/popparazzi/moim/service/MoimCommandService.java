package com.spoons.popparazzi.moim.service;

import com.spoons.popparazzi.moim.dto.command.CreateMoimCommand;
import com.spoons.popparazzi.moim.dto.command.UpdateMoimCommand;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MoimCommandService {

    /**
     * - 최종 등록 시 multipart로 요청이 들어오며,
     *   모임 저장 + 파일 저장 + DB(file_master) insert 까지 한 번에 처리한다.
     *
     * @param command 생성 입력값(도메인 커맨드)
     * @param files 첨부 이미지(최대 5장, 없을 수 있음)
     * @param leaderMemberCode 로그인 유저(방장) 코드
     * @return 생성된 모임 코드(mm_code)
     */
    String create(CreateMoimCommand command, List<MultipartFile> files, String leaderMemberCode);

    // 2. 모임 수정
    String update(UpdateMoimCommand command, List<MultipartFile> files, String memberCode);

    // 3. 모임 삭제
    void delete(String moimCode, String requesterMemberCode);
}