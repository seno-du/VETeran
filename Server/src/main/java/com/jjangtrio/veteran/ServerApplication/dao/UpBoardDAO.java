package com.jjangtrio.veteran.ServerApplication.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.jjangtrio.veteran.ServerApplication.dto.UpBoardDTO;
import java.util.List;
import java.util.Map;

@Mapper
public interface UpBoardDAO {
    // 게시글 목록 조회 (페이징 처리)
    List<Map<String, Object>> getBoardList(@Param("start") Long start, @Param("size") Long size);

    // 총 게시글 수 조회
    Long getTotalCount();

    // 특정 게시글 상세 조회
    Map<String, Object> getBoardDetail(@Param("num") Long num);

    // 게시글 등록
    Long insertBoard(UpBoardDTO board);

    // 게시글 수정
    Long updateBoard(Map<String, Object> board);

    // 게시글 삭제
    Long deleteBoard(@Param("num") Long num);

    // 조회수 증가 (유저가 처음 조회한 경우만)
    void increaseHit(@Param("upboardNum") Long upboardNum, @Param("userNum") Long userNum);

    // 댓글 많은 TOP 5
    List<Map<String, Object>> getTop5BoardsByComments();
}
