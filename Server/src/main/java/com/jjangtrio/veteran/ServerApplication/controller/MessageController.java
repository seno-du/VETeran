package com.jjangtrio.veteran.ServerApplication.controller;

import com.jjangtrio.veteran.ServerApplication.dto.ChatRoomDTO;
import com.jjangtrio.veteran.ServerApplication.dto.ChatRoomMemberDTO;
import com.jjangtrio.veteran.ServerApplication.dto.MessageDTO;
import com.jjangtrio.veteran.ServerApplication.service.ChatRoomService;
import com.jjangtrio.veteran.ServerApplication.service.MessageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "http://localhost:6100")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${message.log.path}")
    private Path logPath;

    @Value("${image.log.path}")
    private String uploadDir;

    @GetMapping("/all")
    public List<MessageDTO> getMessages() {
        return messageService.getAllMessages(); // 전체 메시지를 조회
    }

    @MessageMapping("/chat")
    public void handleChatMessage(MessageDTO message) throws IOException {
        System.out.println("서버가 받은 메시지: " + message.getMessageContent());

        // 먼저 메시지를 파일에 저장 (txt파일에 저장하여 가능)
        saveMessageToFile(message);

        // 메시지를 WebSocket을 통해 즉시 브로드캐스트
        messagingTemplate.convertAndSend("/topic/messages/" + message.getChatroomNum(), message);
        System.out.println("WebSocket으로 메시지 전송 완료! 대상: /topic/messages/" + message.getChatroomNum());

        // 메시지를 DB에 저장
        messageService.saveMessage(message);
    }

    // 특정 채팅방의 메시지 조회
    @GetMapping("/chatroom/{chatroomNum}")
    public List<Map<String, Object>> getMessagesByChatRoom(@PathVariable Long chatroomNum) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long managerNum = Long.valueOf(auth.getPrincipal().toString());

        return messageService.getMessagesByChatRoom(chatroomNum, managerNum);
    }

    private void saveMessageToFile(MessageDTO message) throws IOException {
        Path path = logPath;

        // 디렉토리가 없다면 생성
        if (!Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            String logMessage = message.getChatroomNum() + "," + message.getManagerNum() + "," +
                    message.getMessageContent() + "," + Timestamp.from(Instant.now()) + "," +
                    message.getMessagetype() + "\n";
            writer.write(logMessage);
        }
    }

    @PostMapping("/uploadImage")
    public String uploadImage(@RequestParam("file") MultipartFile file,
            @RequestParam("chatroomNum") Long chatroomNum,
            @RequestParam("managerNum") Long managerNum) throws IOException {
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }

        // 파일 이름에 UUID 추가하여 중복 방지
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        
        Path path = Paths.get(uploadDir, fileName);

        System.out.println("이미지 저장 경로: " + path.toString());

        Files.write(path, file.getBytes());

        // 이미지 URL 생성
        String mediaUrl = "/uploads/" + fileName;
        System.out.println("이미지 업로드 성공! URL: " + mediaUrl);

        // 메시지를 DB에 저장 (이미지 포함)
        MessageDTO imageMessage = new MessageDTO(null, chatroomNum, managerNum, mediaUrl,
                new Timestamp(System.currentTimeMillis()), "이미지", "보냄");

        messageService.saveMessage(imageMessage);

        System.out.println("브로드캐스트 호출: /topic/messages/" + chatroomNum + "로 보내는 메시지: " + imageMessage);

        try {
            messagingTemplate.convertAndSend("/topic/messages/" + chatroomNum, imageMessage);
            System.out.println("메시지 브로드캐스트 성공!");
        } catch (Exception e) {
            System.err.println("메시지 브로드캐스트 실패: " + e.getMessage());
        }

        return mediaUrl; // 클라이언트에 이미지 URL 반환
    }

    @Scheduled(fixedRate = 6000000)
    public void checkAndSaveToDB() throws IOException {
        List<MessageDTO> messages = readMessagesFromFile();
        if (!messages.isEmpty()) {
            messageService.saveMessages(messages); // DB에 일괄 저장
            Files.write(logPath, new byte[0], StandardOpenOption.TRUNCATE_EXISTING); // 파일 초기화
        }
    }

    private List<MessageDTO> readMessagesFromFile() throws IOException {
        List<MessageDTO> messages = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(logPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    try {
                        messages.add(new MessageDTO(null, Long.parseLong(parts[0]), Long.parseLong(parts[1]),
                                parts[2], Timestamp.valueOf(parts[3]), parts[4], "0")); // 상태 0으로 기본 설정
                    } catch (IllegalArgumentException e) {
                        System.err.println("Invalid date format: " + parts[3]);
                    }
                }
            }
        }
        return messages;
    }

    // ----------------------------------------------------------------------------------------------




    // 전체 채팅방 조회
    @GetMapping("/chatrooms")
    public List<ChatRoomDTO> getAllChatRooms() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long managerNum = Long.valueOf(auth.getPrincipal().toString());

        return chatRoomService.getAllChatRooms(managerNum);
    }

    // 채팅방 생성 및 멤버 추가
    @PostMapping("/chatroom/create")
    public ResponseEntity<?> createChatRoom(
            @RequestBody Map<String, Object> params) {
        String chatroomName = (String) params.get("chatroomName");
        String chatroomGroup = (String) params.get("chatroomGroup");
        ChatRoomDTO chatRoomDTO = new ChatRoomDTO();
        chatRoomDTO.setChatroomName(chatroomName);
        chatRoomDTO.setChatroomGroup(chatroomGroup);
        List<Integer> managerNumsI = (List<Integer>) params.get("managerNums");
        List<Long> managerNums = managerNumsI.stream().map(Long::valueOf).collect(Collectors.toList());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long currentManagerNum = Long.valueOf(auth.getPrincipal().toString());
        System.out.println("currentManagerNum" + currentManagerNum);
        // 🔹 현재 로그인한 매니저가 리스트에 없으면 추가
        if (!managerNums.contains(currentManagerNum)) {
            managerNums.add(currentManagerNum);
        }

        // 🔹 채팅방 생성 및 초대
        chatRoomService.createChatRoomAndInviteMembers(currentManagerNum, chatRoomDTO, managerNums);

        return ResponseEntity.ok("채팅방이 생성되고 관리자가 추가되었습니다.");
    }
    

    // 특정 채팅방 멤버 조회
    @GetMapping("/chatroom/{chatroomNum}/members")
    public ResponseEntity<List<ChatRoomMemberDTO>> getChatRoomMembers(@PathVariable Long chatroomNum) {
        List<ChatRoomMemberDTO> members = chatRoomService.getChatRoomMembers(chatroomNum);
        return ResponseEntity.ok(members);
    }

    // 안읽은 메세지 수
    @GetMapping("/chatroom/{chatroomNum}/unreadcount")
    public int getUnreadMessageCount(@PathVariable Long chatroomNum) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long managerNum = Long.valueOf(auth.getPrincipal().toString());

        return messageService.getUnreadMessageCount(chatroomNum, managerNum);
    }

    @PostMapping("/chatroom/{chatroomNum}/read")
    public ResponseEntity<String> markMessagesAsRead(@PathVariable("chatroomNum") Long chatroomNum) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long managerNum = Long.valueOf(auth.getPrincipal().toString());
        messageService.markMessagesAsRead(chatroomNum, managerNum);

        return ResponseEntity.ok("Messages marked as read");
    }

    // 안읽은 전체 메세지 수
    @GetMapping("/unreadcount")
    public int countAllUnreadMessages() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long managerNum = Long.valueOf(auth.getPrincipal().toString());
        return messageService.countAllUnreadMessages(managerNum);
    }

    // 읽은 전체 메세지 수
    @GetMapping("/chatRoom/readcount")
    public int countReadMessages() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long managerNum = Long.valueOf(auth.getPrincipal().toString());
        return messageService.countReadMessages(managerNum);
    }

}
