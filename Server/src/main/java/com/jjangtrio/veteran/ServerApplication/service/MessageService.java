package com.jjangtrio.veteran.ServerApplication.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jjangtrio.veteran.ServerApplication.dao.ManagerDAO;
import com.jjangtrio.veteran.ServerApplication.dao.MessageDAO;
import com.jjangtrio.veteran.ServerApplication.dto.MessageDTO;

@Service
public class MessageService {

    @Autowired
    private MessageDAO messageDAO;

    @Autowired
    private ManagerDAO managerDAO;

    // 전체 메시지 조회
    public List<MessageDTO> getAllMessages() {
        return messageDAO.findAllMessages();
    }

    // 특정 채팅방의 메시지 조회
    public List<Map<String, Object>> getMessagesByChatRoom(Long chatroomNum, Long managerNum) {
        List<MessageDTO> messages = messageDAO.findMessagesByChatRoom(chatroomNum, managerNum);

        List<Map<String, Object>> result = new ArrayList<>();

        for (MessageDTO message : messages) {
            String managerName = managerDAO.findBymanagerNum(message.getManagerNum()); // 🔥 추가된 부분
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("messageNum", message.getMessageNum());
            messageMap.put("chatroomNum", message.getChatroomNum());
            messageMap.put("managerNum", message.getManagerNum());
            messageMap.put("messageContent", message.getMessageContent());
            messageMap.put("messageCreatedAt", message.getMessageCreatedAt());
            messageMap.put("messagetype", message.getMessagetype());
            messageMap.put("messageState", message.getMessageState());
            messageMap.put("managerName", managerName); // 🔥 추가된 부분
            result.add(messageMap);
        }

        return result;
    }

    // 단일 메시지 저장
    @Transactional
    public void saveMessage(MessageDTO message) {
        messageDAO.insertMessages(List.of(message));
    }

    // 여러 메시지 저장
    @Transactional
    public void saveMessages(List<MessageDTO> messages) {
        messageDAO.insertMessages(messages);
    }

    @Transactional
    public int getUnreadMessageCount(Long chatroomNum, Long managerNum) {
        return messageDAO.countUnreadMessages(chatroomNum, managerNum);
    }

    public void markMessagesAsRead(Long chatroomNum, Long managerNum) {
        messageDAO.updateMessagesToRead(chatroomNum, managerNum);
    }

    // 안읽은 전체 메세지 수
    @Transactional
    public int countAllUnreadMessages(Long managerNum) {
        return messageDAO.countAllUnreadMessages(managerNum);
    }

    // 읽은 전체 메세지 수
    @Transactional
    public int countReadMessages(Long managerNum) {
        return messageDAO.countReadMessages(managerNum);
    }
}
