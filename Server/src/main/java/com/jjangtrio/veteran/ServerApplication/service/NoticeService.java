package com.jjangtrio.veteran.ServerApplication.service;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jjangtrio.veteran.ServerApplication.dao.NoticeDAO;
import com.jjangtrio.veteran.ServerApplication.dto.MfileDTO;
import com.jjangtrio.veteran.ServerApplication.dto.NoticeDTO;
import com.jjangtrio.veteran.ServerApplication.dto.PageDTO;

@Service
public class NoticeService {

    @Autowired
    private NoticeDAO noticeDAO;

    // notice 단일조회
    public NoticeDTO selectNotice(Long noticeNum, boolean increaseViewCount) {
        NoticeDTO notice = noticeDAO.selectNotice(noticeNum);
        if (notice != null && increaseViewCount) {
            noticeDAO.updateHit(noticeNum); // 조회수 증가 명시적으로 수행
        }
        return notice;
    }

    // notice 전체 조회
    public List<NoticeDTO> noticeList(PageDTO pageDTO) {

        return noticeDAO.noticeList(pageDTO);
    }

    public long totalCount() {
        return noticeDAO.totalCount();
    }

    // notice 추가
    public void insertNotice(NoticeDTO noticeDTO) {
        noticeDAO.insertNotice(noticeDTO);
    }

    // notice 수정
    public void updateNotice(NoticeDTO noticeDTO) {
        noticeDAO.updateNotice(noticeDTO);
    }

    // 제목으로 검색
    public List<NoticeDTO> searchNoticesByTitle(String noticeTitle) {
        return noticeDAO.searchNoticesByTitle(noticeTitle);
    }

    // 내용으로 검색
    public List<NoticeDTO> searchNoticesByContent(String noticeContent) {
        return noticeDAO.searchNoticesByContent(noticeContent);
    }

    // 날짜로 검색 (특정 날짜의 공지를 조회)
    public List<NoticeDTO> searchNoticesByDate(Date noticeDate) {
        return noticeDAO.searchNoticesByDate(noticeDate);
    }

    // 파일 검색 (날짜, 카테고리, 담당자 기준)
    public List<NoticeDTO> searchNotices(Date noticeDate, String noticeTitle, String noticeContent) {
        return noticeDAO.searchNotices(noticeDate, noticeTitle, noticeContent);
    }

    // notice 활성화/비활성화
    public void statusNotice(NoticeDTO noticeDTO) {
        noticeDAO.statusNotice(noticeDTO);
    }

}