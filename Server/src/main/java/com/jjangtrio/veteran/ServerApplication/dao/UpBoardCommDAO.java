package com.jjangtrio.veteran.ServerApplication.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.jjangtrio.veteran.ServerApplication.dto.UpBoardCommDTO;
import java.util.List;
import java.util.Map;

@Mapper
public interface UpBoardCommDAO {
    // 특정 게시글의 댓글 목록 조회
    List<Map<String, Object>> getCommentList(Long upboardNum);

    // 댓글 삽입
    Long insertComment(UpBoardCommDTO comment);

    // 댓글 비활성화
    void disableComment(@Param("num") Long num);
}