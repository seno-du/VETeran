package com.jjangtrio.veteran.ServerApplication.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jjangtrio.veteran.ServerApplication.dto.MessageDTO;

@Mapper
public interface MessageDAO {
    // 전체 메시지 조회
    List<MessageDTO> findAllMessages();

    // 특정 채팅방의 메시지 조회
    List<MessageDTO> findMessagesByChatRoom(@Param("chatroomNum") Long chatroomNum, @Param("managerNum") Long managerNum);

    // 메시지 저장
    void insertMessages(@Param("list") List<MessageDTO> messages);

    // 메시지 상태 업데이트
    int updateMessageState(@Param("managerNum") Long managerNum, @Param("messageState") String messageState);

    // 메시지 번호로 메시지 조회
    MessageDTO findMessageByNum(@Param("messageNum") Long messageNum);

    // 안읽은 메세지 수
    int countUnreadMessages(@Param("chatroomNum") Long chatroomNum, @Param("managerNum") Long managerNum);

    // 안읽은 메시지 상태 업데이트
    void updateMessagesToRead(@Param("chatroomNum") Long chatroomNum, @Param("managerNum") Long managerNum);

    // 안읽은 전체 메세지 수
    int countAllUnreadMessages(@Param("managerNum") Long managerNum);

    // 읽은 전체 메세지 수
    int countReadMessages(@Param("managerNum") Long managerNum);
}
