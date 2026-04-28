package com.spoons.popparazzi.moim.service;

import com.spoons.popparazzi.moim.dto.command.CreateMoimCommand;
import com.spoons.popparazzi.moim.dto.command.UpdateMoimCommand;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MoimCommandService {

    /**
     * @methodName  : create
     * @author      : seulgi Yang
     * @param       : CreateMoimCommand, files, leaderMemberCode
     * @returnType  : String
     * @desc        : 모임 생성. 모임 저장 + 카테고리 매핑 + 파일 저장 일괄 처리
     */
    String create(CreateMoimCommand command, List<MultipartFile> files, String leaderMemberCode);

    /**
     * @methodName  : update
     * @author      : seulgi Yang
     * @param       : UpdateMoimCommand, files, requesterMemberCode
     * @returnType  : String
     * @desc        : 모임 수정. 카테고리 전체 교체 + 파일 유지/삭제/추가 처리
     */
    String update(UpdateMoimCommand command, List<MultipartFile> files, String requesterMemberCode);

    /**
     * @methodName  : delete
     * @author      : seulgi Yang
     * @param       : moimCode, requesterMemberCode
     * @returnType  : void
     * @desc        : 모임 삭제. 당일 삭제 불가. 첨부파일 삭제 + soft delete 처리
     */
    void delete(String moimCode, String requesterMemberCode);
}
