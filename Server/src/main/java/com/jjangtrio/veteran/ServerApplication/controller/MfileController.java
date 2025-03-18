package com.jjangtrio.veteran.ServerApplication.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.net.URLEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.jjangtrio.veteran.ServerApplication.dto.MfileDTO;
import com.jjangtrio.veteran.ServerApplication.dto.PageDTO;
import com.jjangtrio.veteran.ServerApplication.service.ManagerService;
import com.jjangtrio.veteran.ServerApplication.service.MfileService;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/mfile")

public class MfileController {
    @Value("${image.log.path}")
    private String uploadDir;

    @Autowired
    private MfileService mfileService;

    @Autowired
    private ManagerService managerService;

    // ✅ 단일 파일 조회
    @GetMapping("/detail/{mfileNum}")
    public ResponseEntity<?> getFileDetail(@PathVariable Long mfileNum) {
        MfileDTO mfile = mfileService.selectMfile(mfileNum);
        if (mfile == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("파일 정보를 찾을 수 없습니다.");
        }
        return ResponseEntity.ok(mfile);
    }

    // 파일 다운로드 (상대 경로 적용)
    @GetMapping("/download/{mfileNum}")
    public void downloadFile(@PathVariable Long mfileNum, HttpServletResponse response) {
        MfileDTO mfile = mfileService.selectMfile(mfileNum);
        if (mfile == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            // 현재 작업 디렉토리 확인
            String currentDir = System.getProperty("user.dir");
            System.out.println("현재 작업 디렉토리: " + currentDir);

            // 상대 경로 설정 (VETeran_Back/Server/uploads)
            String relativePath = "uploads";
            String downloadPath = currentDir + File.separator + relativePath;
            Path filePath = Paths.get(downloadPath, mfile.getMfileName());
            File file = filePath.toFile();

            // 파일 존재 여부 확인
            if (!file.exists()) {
                System.out.println("파일을 찾을 수 없습니다: " + filePath);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 다운로드 횟수 증가
            mfileService.increaseDownloadCount(mfileNum);

            // 파일 다운로드 설정
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    "attachment; filename*=UTF-8''" + URLEncoder.encode(mfile.getMfileRealName(), "UTF-8"));
            response.setContentLength((int) file.length());

            // 파일 스트림 처리
            FileInputStream fis = new FileInputStream(file);
            OutputStream os = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
            fis.close();
            os.close();

            System.out.println("파일 다운로드 완료: " + file.getAbsolutePath());

        } catch (IOException e) {
            System.out.println("파일 다운로드 중 오류 발생: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // ✅ 페이징 처리된 파일 목록 조회
    @GetMapping("/all")
    public ResponseEntity<?> getLatestList(@RequestParam(name = "page") long page) {
        System.out.println("Received page number: " + page);
        try {
            Map<String, Object> result = new HashMap<>();

            long count = mfileService.totalCount();
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
            result.put("list", mfileService.mfileList(pdto));

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("실패: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<String> insertMfile(
            @RequestParam(name = "mFile", required = false) MultipartFile mFile,
            @ModelAttribute MfileDTO mfileDTO) {

        try {
            // ✅ 현재 인증된 사용자 정보 가져오기
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
            }

            // ✅ managerNum 가져오기
            Long managerNum = Long.valueOf(auth.getPrincipal().toString());

            // ✅ 매니저 이름 조회 (`getManagerById` 사용)
            Map<String, Object> managerInfo = managerService.getManagerById(managerNum);
            String managerName = managerInfo != null ? (String) managerInfo.get("managerName") : null;

            if (managerName == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("매니저 정보를 찾을 수 없습니다.");
            }

            System.out.println("✅ 현재 로그인된 관리자 번호: " + managerNum);
            System.out.println("✅ 현재 로그인된 관리자 이름: " + managerName);

            // ✅ 작성자 정보 설정
            mfileDTO.setMfileUploader(managerName);

            // ✅ 파일 저장 처리 (상대 경로 적용)
            if (mFile != null && !mFile.isEmpty()) {
                String originalFileName = mFile.getOriginalFilename();
                String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".")); // 확장자 추출
                String newFileName = UUID.randomUUID().toString() + fileExtension; // UUID + 확장자로 파일 이름 변경

                // ✅ 현재 작업 디렉토리 확인
                String currentDir = System.getProperty("user.dir");
                System.out.println("현재 작업 디렉토리: " + currentDir);

                // ✅ 상대 경로 설정 (VETeran_Back/Server/uploads)
                String relativePath = "uploads";
                String uploadDir = currentDir + File.separator + relativePath;
                File uploadPath = new File(uploadDir);

                // ✅ 디렉토리가 없으면 생성
                if (!uploadPath.exists()) {
                    boolean isCreated = uploadPath.mkdirs();
                    if (isCreated) {
                        System.out.println("업로드 디렉토리 생성 성공: " + uploadPath);
                    } else {
                        System.out.println("업로드 디렉토리 생성 실패: " + uploadPath);
                    }
                }

                // ✅ 파일 저장
                File file = new File(uploadPath, newFileName);
                mFile.transferTo(file);
                System.out.println("파일 저장 경로: " + file.getAbsolutePath());

                // ✅ 파일 정보 DTO에 저장
                mfileDTO.setMfileRealName(originalFileName);
                mfileDTO.setMfileName(newFileName);
            }

            // ✅ DB 저장
            mfileService.insertMfile(mfileDTO);

            return ResponseEntity.ok("Upload successful");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + e.getMessage());
        }
    }

    // ✅ 수정할 공지 데이터 불러오기
    @GetMapping("/getAll/{mfileNum}")
    public ResponseEntity<?> getMfileForEdit(@PathVariable Long mfileNum) {
        MfileDTO mfile = mfileService.selectMfile(mfileNum);
        if (mfile != null) {
            return ResponseEntity.ok(mfile);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 파일을을 찾을 수 없습니다.");
        }
    }

    // ✅ 파일 수정
    @PutMapping("/update/{mfileNum}")
    public ResponseEntity<?> updateMfile(
            @RequestParam(name = "mFile", required = false) MultipartFile mFile,
            @ModelAttribute MfileDTO mfileDTO) {

        // ✅ 기존 데이터 조회
        MfileDTO existingMfile = mfileService.selectMfile(mfileDTO.getMfileNum());
        if (existingMfile == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 파일을 찾을 수 없습니다.");
        }

        // ✅ 이미지 변경 처리
        if (mFile != null && !mFile.isEmpty()) {
            try {
                String originalFileName = mFile.getOriginalFilename();
                String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                String newFileName = UUID.randomUUID().toString() + fileExtension;
                File file = new File(uploadDir + "/" + newFileName);
                mFile.transferTo(file);
                mfileDTO.setMfileName(newFileName);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 실패: " + e.getMessage());
            }
        } else {
            // ✅ 이미지 변경 없을 경우 기존 이미지 유지
            mfileDTO.setMfileName(existingMfile.getMfileName());
        }

        // ✅ 수정 실행
        mfileService.updateMfile(mfileDTO);
        return ResponseEntity.ok("파일이 수정되었습니다.");
    }

    // 파일 검색 (날짜, 카테고리, 담당자 기준)
    @GetMapping("/search")
    public ResponseEntity<List<MfileDTO>> searchMfiles(
            @RequestParam(value = "mfileDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date mfileDate,
            @RequestParam(value = "mfileCategory", required = false) Integer mfileCategory,
            @RequestParam(value = "mfileUploader", required = false) String mfileUploader) {

        // 검색 조건이 모두 비어 있으면 BadRequest 응답
        if ((mfileDate == null || mfileDate.toString().isEmpty())
                && (mfileCategory == null || mfileCategory == 0)
                && (mfileUploader == null || mfileUploader.trim().isEmpty())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
        }

        try {
            List<MfileDTO> results = mfileService.searchMfiles(mfileDate, mfileCategory, mfileUploader);

            if (results.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @PutMapping("/status/{mfileNum}")
    public ResponseEntity<?> statusMfile(@PathVariable Long mfileNum) {
        // MfileDTO 객체를 생성하거나 조회하는 로직 추가
        MfileDTO mfileDTO = new MfileDTO();
        mfileDTO.setMfileNum(mfileNum);
        mfileDTO.setMfileStatus("비활성"); // 상태를 설정하는 로직, 필요에 따라 수정 가능

        // 서비스 메서드 호출
        mfileService.statusMfile(mfileDTO);

        return ResponseEntity.ok().body("파일 활성화/비활성화 상태가 변경되었습니다.");
    }

    @GetMapping("/find/{mfileNum}")
    public ResponseEntity<?> findMfile(@PathVariable Long mfileNum) {
        MfileDTO mfile = mfileService.selectMfile(mfileNum);
        if (mfile != null) {
            return ResponseEntity.ok(mfile);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 파일을 찾을 수 없습니다.");
        }
    }

}
