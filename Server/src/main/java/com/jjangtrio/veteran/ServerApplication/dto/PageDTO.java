package com.jjangtrio.veteran.ServerApplication.dto;

import org.apache.ibatis.type.Alias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Alias("pagedto")
public class PageDTO {
    //페이징 처리를 위한 속성
	private long currentPage;       // 현재 페이지 번호
    private long pageSize;          // 한 페이지당 데이터 개수
    private long totalRecords;      // 전체 데이터 개수
    private long totalPages;        // 전체 페이지 수
    private long startIndex;        // 시작 인덱스
    private long endIndex;          // 끝 인덱스
    private boolean hasPrevPage;   // 이전 페이지 존재 여부
    private boolean hasNextPage;   // 다음 페이지 존재 여부
    
}
