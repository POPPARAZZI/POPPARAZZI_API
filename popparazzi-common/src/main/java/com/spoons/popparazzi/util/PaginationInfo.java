package com.spoons.popparazzi.util;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;

@Setter
@Getter
@Schema(description = "페이징 요청 정보")
public class PaginationInfo {
	/**
	 * Required Fields currentPage : 현재 페이지 recordCountPerPage : 페이지당 보여질 레코드수
	 * blockSize : 블럭당 보여질 페이지 수 totalRecord : totalRecord 총 레코드 수
	 */
    private HashMap<String, Object> parameter;

    private List<HashMap<String, Object>> pagingList;

	@Schema(description = "현재 페이지 번호", example = "1")
	private int currentPage = 1; // 현재 페이지
    @Schema(description = "pageSize 페이지당 보여질 레코드수", example = "1")
	private int recordCountPerPage; // pageSize 페이지당 보여질 레코드수
	@Schema(description = "블럭당 보여질 페이지 수", example = "10", hidden = true)
	private int blockSize = 10; // 블럭당 보여질 페이지 수
	@Schema(description = "총 레코드 수", example = "1", hidden = true)
	private int totalRecord; // 총 레코드 수

	public void pageInit() {

		this.setBlockSize(10);

		if (this.getTotalRecord() <= this.getRecordCountPerPage()) {
			this.setCurrentPage(1);
		}

	}

	public void setCurrentPage(int currentPage) {
        this.currentPage = Math.max(currentPage, 1);
	}


    /**
	 * Not Required Fields
	 *
	 */
	@Schema(description = "총 페이지수", example = "1", hidden = true)
	private int totalPage; // 총 페이지수
	@Schema(description = "블럭당 시작 페이지호", example = "1, 11, 21, 31, ...", hidden = true)
	private int firstPage; // 블럭당 시작 페이지, 1, 11, 21, 31, ...
	@Schema(description = "블럭당 마지막 페이지", example = "10, 20, 30, 40, ...", hidden = true)
	private int lastPage; // 블럭당 마지막 페이지 10, 20, 30, 40, ...
	@Schema(description = "페이지당 시작 인덱스", example = "0, 5, 10, 15 ...", hidden = true)
	private int firstRecordIndex; // 페이지당 시작 인덱스 0, 5, 10, 15 ...
	@Schema(description = "페이지당 마지막 인덱스", example = "5,10,15,20....", hidden = true)
	private int lastRecordIndex; // 페이지당 마지막 인덱스 5,10,15,20....

	public int getTotalPage() {

		totalPage = (int) Math.ceil((float) totalRecord / recordCountPerPage);

		return totalPage;
	}

	
	public int getFirstPage() {
		  
		firstPage= currentPage-((currentPage-1)%blockSize);
	  
	  return firstPage;
	  
	}
	

		/*
		 * public int getFirstPage() { if (currentPage <= 1) { return 1; // 현재 페이지가 1보다
		 * 작거나 같으면 첫 번째 페이지를 반환 } else { firstPage = currentPage - ((currentPage - 1) %
		 * blockSize); return firstPage; } }
		 */

	public int getLastPage() {
		lastPage = firstPage + (blockSize - 1);
		if (lastPage > getTotalPage()) {
			lastPage = getTotalPage();
		}
		return lastPage;
	}

	public int getFirstRecordIndex() {
		firstRecordIndex = (getCurrentPage() - 1) * getRecordCountPerPage();
		return firstRecordIndex;
	}

	public int getLastRecordIndex() {
		lastRecordIndex = getCurrentPage() * getRecordCountPerPage();
		return lastRecordIndex;
	}

	@Override
	public String toString() {
		return "PaginationInfo [currentPage=" + currentPage + ", recordCountPerPage=" + recordCountPerPage
				+ ", blockSize=" + blockSize + ", totalRecord=" + totalRecord + ", totalPage=" + totalPage
				+ ", firstPage=" + firstPage + ", lastPage=" + lastPage + ", firstRecordIndex=" + firstRecordIndex
				+ ", lastRecordIndex=" + lastRecordIndex + "]";
	}
}
