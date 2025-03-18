package com.jjangtrio.veteran.ServerApplication.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jjangtrio.veteran.ServerApplication.dto.MfileDTO;
import com.jjangtrio.veteran.ServerApplication.dto.PageDTO;

@Mapper
public interface MfileDAO {

    // mfile 조회
    MfileDTO selectMfile(@Param("mfileNum") Long mfileNum);

    // mfile 전체 조회
    List<MfileDTO> mfileList(PageDTO pageDTO);

    long totalCount();

    // mfile 추가
    void insertMfile(MfileDTO mfileDTO);

    // mfile 다운로드 횟수 증가
    void increaseDownloadCount(@Param("mfileNum") Long mfileNum);

    // mfile 수정
    void updateMfile(MfileDTO mfileDTO);

    // 파일 검색 (날짜, 카테고리, 담당자 기준)
    List<MfileDTO> searchMfiles(
            @Param("mfileDate") Date mfileDate,
            @Param("mfileCategory") Integer mfileCategory,
            @Param("mfileUploader") String mfileUploader);

    // mfile 활성화/비활성화
    void statusMfile(MfileDTO mfileDTO);

}
