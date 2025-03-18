package com.jjangtrio.veteran.ServerApplication.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jjangtrio.veteran.ServerApplication.dao.UpBoardDAO;
import com.jjangtrio.veteran.ServerApplication.dto.UpBoardDTO;

import java.util.List;
import java.util.Map;
@Service
public class UpBoardService {

    @Autowired
    private UpBoardDAO upBoardDAO;

    public List< Map<String, Object>> getBoardList(Long start, Long size) {
        return upBoardDAO.getBoardList(start, size);
    }

    public Long getTotalCount() {
        return upBoardDAO.getTotalCount();
    }

    public Map<String, Object> getBoardDetail(Long upboardNum, Long userNum) {
        if (userNum == null) userNum = -1L;
        upBoardDAO.increaseHit(upboardNum, userNum);
        return upBoardDAO.getBoardDetail(upboardNum);
    }



    // 게시글 등록
    public Long insertBoard(UpBoardDTO board) {
        // 예외 처리 / 파일 저장 로직은 Controller에서 처리하거나, 여기서 처리해도 됨
        Long result = upBoardDAO.insertBoard(board);
        return result != null ? result : -1L;
    }

    public Long updateBoard(Map<String, Object> board) {
        return upBoardDAO.updateBoard(board); 
    }

    public Long deleteBoard(Long num) {
        return upBoardDAO.deleteBoard(num);
    }

    public List< Map<String, Object>> getTop5BoardsByComments() {
        return upBoardDAO.getTop5BoardsByComments();
    }
}
