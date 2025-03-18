package com.jjangtrio.veteran.ServerApplication.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.jjangtrio.veteran.ServerApplication.dto.NoticeDTO;
import com.jjangtrio.veteran.ServerApplication.dto.PageDTO;
import com.jjangtrio.veteran.ServerApplication.service.NoticeService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    @Value("${image.log.path}")
    private String uploadDir;

    @Autowired
    private NoticeService noticeService;

    // ✅ 단일 공지 조회
    @GetMapping("/detail/{noticeNum}")
    public ResponseEntity<?> getNoticeDetail(@PathVariable Long noticeNum) {
        NoticeDTO notice = noticeService.selectNotice(noticeNum, true); // 조회수 증가
        return notice != null ? ResponseEntity.ok(notice)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("공지사항을 찾을 수 없습니다.");
    }

    // ✅ 페이징 처리된 공지 목록 조회
    @GetMapping("/all")
    public ResponseEntity<?> getLatestList(@RequestParam(name = "page") long page) {
        System.out.println("Received page number: " + page);
        System.out.println("현재 작업 디렉토리: " + System.getProperty("user.dir"));

        try {
            Map<String, Object> result = new HashMap<>();

            long count = noticeService.totalCount();
            System.out.println("count : " + count);
            PageDTO pdto = PageDTO.builder()
                    .currentPage(page)
                    .pageSize(5L)
                    .totalRecords(count)
                    .totalPages((long) (Math.ceil((double) count / 5))) // 올림 처리
                    .startIndex((long) ((page - 1) * 5 + 1)) // 시작 인덱스 계산
                    .endIndex((long) Math.min(page * 5, count)) // 끝 인덱스 계산 (pageSize 사용)
                    .hasPrevPage(page > 1) // 이전 페이지 여부
                    .hasNextPage(page < (int) Math.ceil((double) count / 5)) // 다음 페이지 여부
                    .build();
            System.out.println("count : " + count);
            result.put("PageDTO", pdto);
            result.put("list", noticeService.noticeList(pdto));

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("실패: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> insertNotice(@RequestParam(name = "img", required = false) MultipartFile img,
            @ModelAttribute NoticeDTO noticeDTO,
            HttpServletRequest request) throws IOException {

        // 이미지 업로드 URL
        String mediaUrl = null;

        // 이미지가 있는 경우
        if (img != null && !img.isEmpty()) {
            String originalFileName = img.getOriginalFilename();
            String newFileName = UUID.randomUUID().toString() + "_" + originalFileName;

            // 업로드할 디렉토리 경로 설정
            String uploadDir = System.getProperty("user.dir") + File.separator
                    + "Veteran_Back\\Server\\src\\main\\resources\\static\\uploads";
            System.out.println("경로========================" + uploadDir);
            File uploadPath = new File(uploadDir);

            // // 디렉토리가 없으면 생성
            // if (!uploadPath.exists()) {
            // uploadPath.mkdirs(); // 디렉토리가 없다면 생성
            // }

            // 파일 경로 생성
            Path path = Paths.get(uploadDir, newFileName);
            Files.write(path, img.getBytes()); // 파일 저장

            // 이미지 URL 생성
            mediaUrl = "/uploads/" + newFileName;

            // NoticeDTO에 파일명 저장
            noticeDTO.setNoticeImage(newFileName);
        }

        // 공지사항 정보 저장
        noticeService.insertNotice(noticeDTO);

        // 응답 반환
        if (mediaUrl != null) {
            return ResponseEntity.ok("Upload successful, Image URL: " + mediaUrl);
        } else {
            return ResponseEntity.ok("Notice added successfully without file");
        }
    }

    // ✅ 수정할 공지 데이터 불러오기 (조회수 증가 X)
    @GetMapping("/getAll/{noticeNum}")
    public ResponseEntity<?> getNoticeForEdit(@PathVariable Long noticeNum) {
        NoticeDTO notice = noticeService.selectNotice(noticeNum, false);
        if (notice != null) {
            return ResponseEntity.ok(notice);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 공지를 찾을 수 없습니다.");
        }
    }

    @PutMapping("/update/{noticeNum}")
    public ResponseEntity<?> updateNotice(
            @RequestParam(name = "img", required = false) MultipartFile img,
            @ModelAttribute NoticeDTO noticeDTO) throws IOException {

        // ✅ 기존 데이터 조회
        NoticeDTO existingNotice = noticeService.selectNotice(noticeDTO.getNoticeNum(), false);
        if (existingNotice == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 공지를 찾을 수 없습니다.");
        }

        // ✅ 이미지 업로드 경로 설정 (insertNotice와 동일하게 적용)
        String mediaUrl = null;
        if (img != null && !img.isEmpty()) {
            String originalFileName = img.getOriginalFilename();
            String newFileName = UUID.randomUUID().toString() + "_" + originalFileName;

            // ✅ 업로드할 디렉토리 경로 설정
            String uploadDir = System.getProperty("user.dir") + File.separator
                    + "Veteran_Back/Server/src/main/resources/static/uploads";
            File uploadPath = new File(uploadDir);

            // ✅ 디렉토리가 없으면 생성
            if (!uploadPath.exists()) {
                uploadPath.mkdirs();
            }

            // ✅ 파일 경로 생성 후 저장
            Path path = Paths.get(uploadDir, newFileName);
            Files.write(path, img.getBytes());

            // ✅ 새로운 이미지로 변경
            mediaUrl = "/uploads/" + newFileName;
            noticeDTO.setNoticeImage(newFileName);
        } else {
            // ✅ 이미지 변경 없을 경우 기존 이미지 유지
            noticeDTO.setNoticeImage(existingNotice.getNoticeImage());
        }

        // ✅ 기존 조회수 & 날짜 유지
        noticeDTO.setNoticeHit(existingNotice.getNoticeHit());
        noticeDTO.setNoticeDate(existingNotice.getNoticeDate());

        // ✅ 공지사항 업데이트 실행
        noticeService.updateNotice(noticeDTO);

        // ✅ 응답 반환
        if (mediaUrl != null) {
            return ResponseEntity.ok("Notice updated successfully, Image URL: " + mediaUrl);
        } else {
            return ResponseEntity.ok("Notice updated successfully without file change");
        }
    }

    @GetMapping("/search/title")
    public ResponseEntity<List<NoticeDTO>> searchNoticesByTitle(@RequestParam("noticeTitle") String noticeTitle) {
        try {
            if (noticeTitle == null || noticeTitle.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.emptyList());
            }

            // 로그 추가: noticeTitle 값 확인
            System.out.println("Searching notices with title: " + noticeTitle);

            List<NoticeDTO> results = noticeService.searchNoticesByTitle(noticeTitle);

            if (results.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            // 예외 발생 시 로그를 추가하여 원인 확인
            e.printStackTrace(); // 콘솔에 스택 트레이스를 출력하여 오류 원인 파악
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // 내용으로 검색
    @GetMapping("/search/content")
    public ResponseEntity<List<NoticeDTO>> searchNoticesByContent(@RequestParam("noticeContent") String noticeContent) {
        try {
            if (noticeContent == null || noticeContent.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.emptyList());
            }

            List<NoticeDTO> results = noticeService.searchNoticesByContent(noticeContent);

            if (results.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // 날짜로 검색
    @GetMapping("/search/date")
    public ResponseEntity<List<NoticeDTO>> searchNoticesByDate(
            @RequestParam("noticeDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date noticeDate) {
        try {
            if (noticeDate == null) {
                return ResponseEntity.badRequest().body(Collections.emptyList());
            }
            List<NoticeDTO> results = noticeService.searchNoticesByDate(noticeDate);
            if (results.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // notice 검색_manager단 (날짜, 제목, 내용 기준)
    @GetMapping("/find")
    public ResponseEntity<List<NoticeDTO>> searchNotices(
            @RequestParam(value = "noticeDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date noticeDate,
            @RequestParam(value = "noticeTitle", required = false) String noticeTitle,
            @RequestParam(value = "noticeContent", required = false) String noticeContent) {
        System.out.println("noticeDate: " + noticeDate);
        System.out.println("noticeTitle: " + noticeTitle);
        System.out.println("noticeContent: " + noticeContent);
                System.out.println("=================================================");
        // 검색 조건이 모두 비어 있으면 BadRequest 응답
        if ((noticeDate == null || noticeDate.toString().isEmpty())
                && (noticeTitle == null || noticeTitle.trim().isEmpty())
                && (noticeContent == null || noticeContent.trim().isEmpty())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
        }

        try {
            List<NoticeDTO> results = noticeService.searchNotices(noticeDate, noticeTitle, noticeContent);

            if (results.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // ✅ 공지 활성화/비활성화
    @PutMapping("/status/{noticeNum}")
    public ResponseEntity<?> statusNotice(@PathVariable Long noticeNum) {
        NoticeDTO notice = noticeService.selectNotice(noticeNum, false);
        if (notice == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 공지를 찾을 수 없습니다.");
        }
        noticeService.statusNotice(notice);
        return ResponseEntity.ok("공지사항의 활성화/비활성화가 완료되었습니다.");
    }
}
