package com.jjangtrio.veteran.ServerApplication.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jjangtrio.veteran.ServerApplication.dto.UserDTO;
import com.jjangtrio.veteran.ServerApplication.service.ChataiService;
import com.jjangtrio.veteran.ServerApplication.service.UserService;


@RestController
@RequestMapping("/api/chatbot")
public class ChataiController {

    @Autowired
    private ChataiService chatbotService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<String> chat(@RequestBody Map<String, String> request) {

        // message: messageString,
        // count : messages.length,
        // userNum : user.userNum

        String userMessage = request.get("message");
        
        if(chatbotService.getChatbotResponse(userMessage) == null)
            return ResponseEntity.status(500).body("대화 내용을 찾을 수 없습니다.");

        if(request.get("userNum").equals(0) && request.get("count").equals(11))
            return ResponseEntity.status(500).body("로그인이 필요한 서비스입니다.");

        return ResponseEntity.status(200).body(chatbotService.getChatbotResponse(userMessage));
    }

    @PostMapping("/saveChatHistory")
    public ResponseEntity<String> saveChatHistory(@RequestBody List<Map<String, Object>> chatList) {
        try {

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userNum = Long.valueOf(auth.getPrincipal().toString());

            UserDTO user = userService.selectUserNum(userNum);
            if (user == null || user.getUserStatus().equals("비활성")) 
                return ResponseEntity.status(406).body("사용자 정보가 없습니다.");
                
            if(chatbotService.saveChatHistory(userNum, chatList))
                return ResponseEntity.status(200).body("대화 내용 백업 성공");

            return ResponseEntity.status(500).body("대화 내용 백업 실패.");

        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/getChatHistory/{day}")
    public ResponseEntity<?> getChatHistory(@PathVariable("day") String day) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userNum = Long.valueOf(auth.getPrincipal().toString());

            UserDTO user = userService.selectUserNum(userNum);
            if (user == null || user.getUserStatus().equals("비활성")) 
                return ResponseEntity.status(406).body("사용자 정보가 없습니다.");

            LocalDate date = LocalDate.parse(day, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate newDate = date.plusDays(1);
            String updatedDate = newDate.toString();

            System.out.println(updatedDate);

                Map<String, Object> map = new HashMap<>();
                map.put("userNum", userNum);
                map.put("startDate", day);
                map.put("endDate", updatedDate);

            List<Map<String, Object>> chatHistory = chatbotService.getChatHistoryOneDay(map);

            String chatSummary = chatbotService.chatSummary(chatHistory);

            Map<String, Object> result = new HashMap<>();
            result.put("chatHistory", chatHistory);
            result.put("chatSummary", chatSummary);

            return ResponseEntity.status(200).body(result);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/getChatDateInfo")
    public ResponseEntity<?> getChatDateInfo() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userNum = Long.valueOf(auth.getPrincipal().toString());

            UserDTO user = userService.selectUserNum(userNum);
            if (user == null || user.getUserStatus().equals("비활성")) 
                return ResponseEntity.status(406).body("사용자 정보가 없습니다.");

            List<String> dateList = chatbotService.getChatDateInfo(userNum);

            Set<String> set = new HashSet<>(dateList);
            List<String> uniqueList = new ArrayList<>(set); // 중복 데이터 삭제

            return ResponseEntity.status(200).body(uniqueList);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

}