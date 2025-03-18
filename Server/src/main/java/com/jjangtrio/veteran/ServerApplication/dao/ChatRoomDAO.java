package com.jjangtrio.veteran.ServerApplication.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jjangtrio.veteran.ServerApplication.dto.ChatRoomDTO;
import com.jjangtrio.veteran.ServerApplication.dto.ChatRoomMemberDTO;

@Mapper
public interface ChatRoomDAO {
    // 전체 채팅방 조회
    List<ChatRoomDTO> findAllChatRooms(@Param("managerNum") Long managerNum);

    // 채팅방 번호로 채팅방 조회
    ChatRoomDTO findChatRoomById(@Param("chatroomNum") Long chatroomNum, @Param("managerNum") Long managerNum);

    // 채팅방 생성
    int createChatRoom(@Param("managerNum") Long managerNum, @Param("chatroomName") String chatroomName, @Param("chatroomGroup") String chatroomGroup);

    // 가장 최근에 생성된 chatroomNum 가져오기
    Long getLastInsertedId();

    // 채팅방에 멤버 추가
    // int addMemberToChatRoom(@Param("chatroomNum") Long chatroomNum, @Param("managerNum") Long managerNum);
    int addMemberToChatRoom(Map<String, String> map);
    // 특정 채팅방의 멤버 목록 조회
    List<ChatRoomMemberDTO> findMembersByChatRoom(@Param("chatroomNum") Long chatroomNum);
}
