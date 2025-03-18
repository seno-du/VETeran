package com.jjangtrio.veteran.ServerApplication.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jjangtrio.veteran.ServerApplication.dao.ChatRoomDAO;
import com.jjangtrio.veteran.ServerApplication.dto.ChatRoomDTO;
import com.jjangtrio.veteran.ServerApplication.dto.ChatRoomMemberDTO;

@Service
public class ChatRoomService {

    @Autowired
    private ChatRoomDAO chatRoomDAO;

    // 전체 채팅방 조회
    public List<ChatRoomDTO> getAllChatRooms(Long managerNum) {
        return chatRoomDAO.findAllChatRooms(managerNum);
    }

    @Transactional
    public void createChatRoomAndInviteMembers(Long managerNum,ChatRoomDTO chatRoomDTO, List<Long> managerNums) {
        // 🔹 채팅방 생성
        chatRoomDAO.createChatRoom(managerNum, chatRoomDTO.getChatroomName(), chatRoomDTO.getChatroomGroup());

        // 🔹 생성된 chatroomNum 가져오기
        Long chatroomNum = chatRoomDAO.getLastInsertedId();
        // 🔹 멤버 추가
        for (Long num : managerNums) {
            Map<String, String> map = new HashMap<>();
            map.put("chatroomNum", chatroomNum.toString());
            map.put("managerNum", num.toString());
            chatRoomDAO.addMemberToChatRoom(map);
        }
    }

    // 채팅방의 멤버 목록 조회
    public List<ChatRoomMemberDTO> getChatRoomMembers(Long chatroomNum) {
        return chatRoomDAO.findMembersByChatRoom(chatroomNum);
    }

    // 채팅방 조회
    public ChatRoomDTO getChatRoom(Long chatroomNum, Long managerNum) {
        return chatRoomDAO.findChatRoomById(chatroomNum, managerNum);
    }
}
