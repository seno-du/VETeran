package com.jjangtrio.veteran.ServerApplication.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.jjangtrio.veteran.ServerApplication.dto.UpBoardCommDTO;
import com.jjangtrio.veteran.ServerApplication.service.UpBoardCommService;
// 예: 유저 정보를 조회하는 Service
import com.jjangtrio.veteran.ServerApplication.service.UserService;
import com.jjangtrio.veteran.ServerApplication.dto.UserDTO;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/upboard/comment")
public class UpBoardCommController {

    @Autowired
    private UpBoardCommService upBoardCommService;

    // (예시) 유저 조회를 위한 서비스
    @Autowired
    private UserService userService;

    // 댓글 목록 조회
    @GetMapping("/list/{upboardNum}")
    public ResponseEntity<?> getCommentList(@PathVariable("upboardNum") Long upboardNum) {
        List<Map<String, Object>> comments = upBoardCommService.getCommentList(upboardNum);
        return ResponseEntity.ok(comments);
    }

    // 댓글 등록
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> insertComment(
            @RequestBody UpBoardCommDTO comment,
            HttpServletRequest request) {
        System.out.println(">>>> insertComment() 메서드 진입");

        String userIp = request.getHeader("X-Forwarded-For");
        if (userIp == null || userIp.isEmpty()) {
            userIp = request.getRemoteAddr();
        }

        // 인증 정보
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(403).body(Map.of("status", "fail", "message", "로그인이 필요합니다."));
        }

        try {
            // (1) principal에서 userNum 추출
            Long userNum = Long.valueOf(auth.getPrincipal().toString());

            // (2) DB에서 유저 정보 조회
            UserDTO user = userService.selectUserNum(userNum);
            if (user == null) {
                return ResponseEntity.status(406).body(Map.of("status", "fail", "message", "사용자를 찾을 수 없습니다."));
            }

            // (3) comment에 사용자 정보 세팅
            comment.setUserNum(userNum);
            comment.setUpboardCommReip(userIp);

            // (4) Service 호출
            upBoardCommService.insertComment(comment);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "fail", "message", "댓글 등록 중 오류가 발생했습니다."));
        }
    }

    // 대댓글 등록
    @PostMapping("/reply")
    public ResponseEntity<Map<String, Object>> insertReply(
            @RequestBody UpBoardCommDTO comment,
            HttpServletRequest request) {
        String userIp = request.getHeader("X-Forwarded-For");
        if (userIp == null || userIp.isEmpty()) {
            userIp = request.getRemoteAddr();
        }

        // 인증 정보
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(403).body(Map.of("status", "fail", "message", "로그인이 필요합니다."));
        }

        try {
            // userNum 추출
            Long userNum = Long.valueOf(auth.getPrincipal().toString());

            // 유저 정보 조회
            UserDTO user = userService.selectUserNum(userNum);
            if (user == null) {
                return ResponseEntity.status(406).body(Map.of("status", "fail", "message", "사용자를 찾을 수 없습니다."));
            }

            // comment에 세팅
            comment.setUserNum(userNum);
            comment.setUpboardCommReip(userIp);

            // Service 호출
            upBoardCommService.insertComment(comment);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "fail", "message", "대댓글 등록 중 오류가 발생했습니다."));
        }
    }

    // 댓글 비활성화
    @PutMapping("/disable/{commentNum}")
    public ResponseEntity<?> disableComment(@PathVariable Long commentNum) {
        upBoardCommService.disableComment(commentNum);
        return ResponseEntity.ok().body("댓글이 비활성화되었습니다.");
    }
}