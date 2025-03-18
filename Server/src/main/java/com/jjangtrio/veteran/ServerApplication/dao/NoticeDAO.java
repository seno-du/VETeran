package com.jjangtrio.veteran.ServerApplication.dao;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.jjangtrio.veteran.ServerApplication.dto.NoticeDTO;
import com.jjangtrio.veteran.ServerApplication.dto.PageDTO;

@Mapper
public interface NoticeDAO {

    // notice 조회
    NoticeDTO selectNotice(@Param("noticeNum") Long noticeNum);

    // notice 전체 조회
    List<NoticeDTO> noticeList(PageDTO pageDTO);

    long totalCount();

    // notice 조회수 증가
    void updateHit(@Param("noticeNum") Long noticeNum);

    // notice 추가
    void insertNotice(NoticeDTO noticeDTO);

    // notice 수정
    void updateNotice(NoticeDTO noticeDTO);

    // notice: 제목으로 검색
    List<NoticeDTO> searchNoticesByTitle(@Param("noticeTitle") String noticeTitle);

    // notice: 내용으로 검색
    List<NoticeDTO> searchNoticesByContent(@Param("noticeContent") String noticeContent);

    // notice: 날짜로 검색
    List<NoticeDTO> searchNoticesByDate(@Param("noticeDate") Date noticeDate);

    // notice 검색_manager단 (날짜, 제목, 내용 기준)
    List<NoticeDTO> searchNotices(
            @Param("noticeDate") Date noticeDate,
            @Param("noticeTitle") String noticeTitle,
            @Param("noticeContent") String noticeContent);

    // notice 활성화/비활성화
    void statusNotice(NoticeDTO noticeDTO);
}