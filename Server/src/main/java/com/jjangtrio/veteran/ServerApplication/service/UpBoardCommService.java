package com.jjangtrio.veteran.ServerApplication.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jjangtrio.veteran.ServerApplication.dao.UpBoardCommDAO;
import com.jjangtrio.veteran.ServerApplication.dto.UpBoardCommDTO;

import java.util.List;
import java.util.Map;

@Service
public class UpBoardCommService {

    @Autowired
    private UpBoardCommDAO upBoardCommDAO;

    // 댓글 목록 조회
    public List<Map<String, Object>> getCommentList(Long upboardNum) {
        return upBoardCommDAO.getCommentList(upboardNum);
    }

    // 댓글/대댓글 등록
    @Transactional
    public Long insertComment(UpBoardCommDTO comment) {
        if (comment.getParentNum() == null) {
            comment.setParentNum(0L); // 부모 댓글이 없으면 0
        }
        // Controller에서 이미 comment.setUserNum(), setUserId() 등을 해줬다고 가정

        // DB에 삽입
        return upBoardCommDAO.insertComment(comment);
    }

    // 댓글 비활성화
    public void disableComment(Long commentNum) {
        upBoardCommDAO.disableComment(commentNum);
    }
}