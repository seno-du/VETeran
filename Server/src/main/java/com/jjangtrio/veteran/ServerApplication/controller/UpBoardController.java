package com.jjangtrio.veteran.ServerApplication.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.jjangtrio.veteran.ServerApplication.dto.UpBoardDTO;
import com.jjangtrio.veteran.ServerApplication.dto.UserDTO;
import com.jjangtrio.veteran.ServerApplication.service.UpBoardService;
import com.jjangtrio.veteran.ServerApplication.service.UserService;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/upboard")
public class UpBoardController {

    @Autowired
    private UpBoardService upBoardService;

    @Autowired
    private UserService userService;

    @Value("${image.log.path:}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        if (uploadDir.isEmpty()) {
            uploadDir = "D:/projectFinal/Veteran_Back/Server/src/main/resources/static/uploads"; // 변경된 경로
        }
        File uploadPath = new File(uploadDir);
        if (!uploadPath.exists()) {
            boolean isCreated = uploadPath.mkdirs();
            System.out.println("✅ 업로드 디렉토리 생성됨: " + uploadPath.getAbsolutePath());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> insertBoard(
            @RequestParam("upboardTitle") String upboardTitle,
            @RequestParam("upboardContent") String upboardContent,
            @RequestParam(value = "upboardImgn", required = false) MultipartFile file) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userNum = Long.valueOf(auth.getPrincipal().toString());

            UserDTO user = userService.selectUserNum(userNum);
            if (user == null) {
                return ResponseEntity.status(403).body(Map.of("status", "fail", "message", "로그인이 필요합니다."));
            }

            if (upboardTitle == null || upboardTitle.trim().isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("status", "fail", "message", "제목을 입력하세요."));
            }

            if (upboardContent == null || upboardContent.trim().isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("status", "fail", "message", "내용을 입력하세요."));
            }

            UpBoardDTO board = UpBoardDTO.builder()
                    .userNum(userNum)
                    .upboardTitle(upboardTitle)
                    .upboardContent(upboardContent)
                    .build();

            String savedPath = saveFile(file);
            if (savedPath != null) {
                board.setUpboardImgn(savedPath);
            }

            Long result = upBoardService.insertBoard(board);
            if (result <= 0) {
                return ResponseEntity.status(500).body(Map.of("status", "fail", "message", "게시글 저장 실패"));
            }

            return ResponseEntity
                    .ok(Map.of("status", "success", "message", "게시글 저장 성공", "boardId", result, "imgPath", savedPath));
        } catch (Exception e) {
            e.printStackTrace(); // 예외 출력
            return ResponseEntity.status(500)
                    .body(Map.of("status", "fail", "message", "서버 오류", "error", e.getMessage()));
        }
    }

    private String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "";
        }

        try {
            // WebConfig에서 설정한 경로로 저장
            String uploadPath = uploadDir; // uploadDir을 사용해 경로 설정
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                dir.mkdirs(); // 디렉토리 생성
            }

            String uuid = UUID.randomUUID().toString();
            String safeFileName = uuid + "_" + file.getOriginalFilename();
            File dest = new File(dir, safeFileName);

            file.transferTo(dest);
            return "/uploads/" + safeFileName;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getBoardList(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size) {

        Long start = (page - 1) * size;
        List<Map<String, Object>> list = upBoardService.getBoardList(start, size);
        Long totalCount = upBoardService.getTotalCount();

        return ResponseEntity.ok(Map.of(
                "data", list,
                "totalItems", totalCount,
                "currentPage", page,
                "totalPages", (int) Math.ceil((double) totalCount / size)));
    }

    @GetMapping("/detail/{num}")
    public ResponseEntity<?> getBoardDetail(
            @PathVariable Long num) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userNum = Long.valueOf(auth.getPrincipal().toString());

        Map<String, Object> upboard = upBoardService.getBoardDetail(num, userNum);
        if (upboard == null) {
            return ResponseEntity.status(404).body(Map.of("status", "fail", "message", "게시글을 찾을 수 없습니다."));
        }

        return ResponseEntity.ok(Map.of("status", "success", "data", upboard));
    }

    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateBoard(
            @RequestParam("upboardNum") Long upboardNum,
            @RequestParam("upboardTitle") String upboardTitle,
            @RequestParam("upboardContent") String upboardContent,
            @RequestPart(value = "upboardImgn", required = false) MultipartFile file) {

        // 인증 정보 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userNum = Long.valueOf(auth.getPrincipal().toString());

        // 게시글 상세 정보를 조회
        Map<String, Object> board = upBoardService.getBoardDetail(upboardNum, userNum);
        if (board == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("status", "fail", "message", "게시글을 찾을 수 없습니다."));
        }

        // 게시글 소유자 확인
        Integer boardUserNumInt = (Integer) board.get("userNum");
        Long boardUserNum = boardUserNumInt.longValue();
        if (!userNum.equals(boardUserNum)) {
            return ResponseEntity.status(403)
                    .body(Map.of("status", "fail", "message", "본인만 수정할 수 있습니다."));
        }

        // 게시글 데이터 업데이트
        board.put("upboardTitle", upboardTitle);
        board.put("upboardContent", upboardContent);

        // 파일이 존재하면 저장 후 경로 업데이트
        if (file != null && !file.isEmpty()) {
            String savedPath = saveFile(file);
            board.put("upboardImgn", savedPath);
        }

        // 게시글 수정 처리
        Long result = upBoardService.updateBoard(board);
        if (result > 0) {
            return ResponseEntity.ok(Map.of("status", "success", "message", "게시글이 수정되었습니다."));
        } else {
            return ResponseEntity.status(500)
                    .body(Map.of("status", "fail", "message", "게시글 수정 실패"));
        }
    }

    @DeleteMapping("/delete/{upboardNum}")
    public ResponseEntity<?> deleteBoard(@PathVariable Long upboardNum) {
        // 현재 로그인한 사용자 userNum 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userNum = Long.valueOf(auth.getPrincipal().toString());

        // 삭제할 게시글 정보 조회 (작성자 확인)
        Map<String, Object> board = upBoardService.getBoardDetail(upboardNum, userNum);
        if (board == null) {
            return ResponseEntity.status(404).body(Map.of("status", "fail", "message", "게시글을 찾을 수 없습니다."));
        }

        // 게시글 작성자가 아닌 경우 삭제 불가

        Integer boardUserNumInt = (Integer) board.get("userNum");
        Long boardUserNum = boardUserNumInt.longValue();
        if (!userNum.equals(boardUserNum)) {
            return ResponseEntity.status(403).body(Map.of("status", "fail", "message", "본인만 삭제할 수 있습니다."));
        }

        // 게시글 삭제
        Long result = upBoardService.deleteBoard(upboardNum);

        if (result > 0) {
            return ResponseEntity.ok(Map.of("status", "success", "message", "게시글 삭제 완료"));
        } else {
            return ResponseEntity.status(500).body(Map.of("status", "fail", "message", "게시글 삭제 실패"));
        }
    }

    @GetMapping("/top5")
    public ResponseEntity<?> getTop5Boards() {
        return ResponseEntity.ok(upBoardService.getTop5BoardsByComments());
    }
}
