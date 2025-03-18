package com.jjangtrio.veteran.ServerApplication.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.jjangtrio.veteran.ServerApplication.dto.ManagerDTO;
import com.jjangtrio.veteran.ServerApplication.dto.PageDTO;
import com.jjangtrio.veteran.ServerApplication.dto.PermissionDTO;
import com.jjangtrio.veteran.ServerApplication.service.ManagerService;
import com.jjangtrio.veteran.ServerApplication.service.PermissionService;

@RestController
@RequestMapping("/api/managers")
public class ManagerController {

    @Autowired
    private ManagerService managerService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${image.log.path}")
    private String imageLogPath;

    @GetMapping("/managerPlus")
    public ResponseEntity<?> getManagerPlus() {
        return ResponseEntity.ok(managerService.getAllManagers());
    }

    /** 모든 Manager 조회 */
    @GetMapping("/manager/all")
    public ResponseEntity<?> getManagerList(@RequestParam(name = "page") long page) {
        try {
            Map<String, Object> result = new HashMap<>();

            long count = managerService.getTotalCount(); // 총 매니저 수 조회
            PageDTO pdto = PageDTO.builder()
                    .currentPage(page)
                    .pageSize(10L)
                    .totalRecords(count)
                    .totalPages((long) (Math.ceil((double) count / 10))) // 올림 처리
                    .startIndex((long) ((page - 1) * 10)) // 시작 인덱스 계산
                    .endIndex((long) Math.min(page * 10, count)) // 끝 인덱스 계산 (pageSize 사용)
                    .hasPrevPage(page > 1) // 이전 페이지 여부
                    .hasNextPage(page < (int) Math.ceil((double) count / 10)) // 다음 페이지 여부
                    .build();
            result.put("PageDTO", pdto);
            result.put("list", managerService.getAllManagersWithPermissions(pdto));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("실패: " + e.getMessage());
        }
    }

    /** 특정 Manager 조회 */
    @GetMapping("/manager/{managerNum}")
    public ResponseEntity<?> getManagerNum(@PathVariable Long managerNum) {
        return ResponseEntity.ok(managerService.getManagerById(managerNum));
    }

    public static String generateRandomPassword(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        Random random = new Random();
        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            password.append(characters.charAt(index));
        }

        return password.toString();
    }

    /** Manager 추가 */
    @PostMapping("/add")
    public ResponseEntity<?> addManager(
            @RequestParam(name = "managerImage", required = false) MultipartFile managerImage,
            @RequestParam(value = "managerName") String managerName,
            @RequestParam(value = "managerLicenseNum") String managerLicenseNum,
            @RequestParam(value = "managerPhone") String managerPhone,
            @RequestParam(value = "managerEmail") String managerEmail,
            @RequestParam(value = "managerBirth") String managerBirth,
            @RequestParam(value = "managerGender") String managerGender,
            @RequestParam(value = "managerAddress") String managerAddress,
            @RequestParam(value = "permissionRole") String permissionRole,
            @RequestParam(value = "role") String role) {

        try {
            // 전화번호 유효성 검사 (000-0000-0000 형식으로 제한)
            if (!managerPhone.matches("^\\d{3}-\\d{4}-\\d{4}$")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("전화번호는 000-0000-0000 형식으로 입력해야 합니다.");
            }

            // 이메일 형식 검증
            if (!managerEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("유효한 이메일 형식이 아닙니다.");
            }

            // 생년월일 변환
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date managerBirth1 = dateFormat.parse(managerBirth);

            // 매니저 ID 생성
            String managerId = managerService.selectManagerIdCount(role);
            String managerPwd = generateRandomPassword(10);
            String managerEncodingPwd = passwordEncoder.encode(managerPwd);

            System.out.println(imageLogPath);

            String mediaUrl = null;
            if (managerImage != null && !managerImage.isEmpty()) {
                String originalFileName = managerImage.getOriginalFilename();
                String newFileName = UUID.randomUUID().toString() + "_" + originalFileName;

                    // 업로드할 디렉토리 경로 설정
                    String uploadDir = System.getProperty("user.dir") + File.separator + "Veteran_Back\\Server\\src\\main\\resources\\static\\uploads";
                    System.out.println("경로========================" + uploadDir);
                    File uploadPath = new File(uploadDir);
                    
                if (!uploadPath.exists()) {
                    uploadPath.mkdirs(); // 디렉토리가 없다면 생성
                }

                Path path = Paths.get(uploadDir, newFileName);
                Files.write(path, managerImage.getBytes());

                // 이미지 URL 생성
                mediaUrl = "/uploads/" + newFileName;
            }

            // 매니저 객체 생성 및 저장
            ManagerDTO manager = new ManagerDTO();
            manager.setManagerName(managerName);
            manager.setManagerLicenseNum(managerLicenseNum);
            manager.setManagerId(managerId);
            manager.setManagerPwd(managerEncodingPwd);
            manager.setManagerPhone(managerPhone);
            manager.setManagerEmail(managerEmail);
            manager.setManagerBirth(managerBirth1);
            manager.setManagerGender(managerGender);
            manager.setManagerAddress(managerAddress);
            manager.setManagerImage(mediaUrl);
            System.out.println(manager);

            // 매니저 정보 저장
            managerService.insertManager(manager);

            Long managerNumber = managerService.selectXCount();

            // 권한 추가
            PermissionDTO permission = new PermissionDTO();
            permission.setManagerNum(managerNumber);
            permission.setPermissionRole(permissionRole);
            permission.setPermissionState("직원");
            permission.setPermissionPasswordless("미등록");

            permissionService.insertPermission(permission);

            // 응답 데이터 구성
            Map<String, Object> result = new HashMap<>();
            result.put("managerEmail", managerEmail);
            result.put("managerPwd", managerPwd);

            return ResponseEntity.ok(result);

        } catch (ParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘못된 생년월일 형식입니다.");
        } catch (io.jsonwebtoken.io.IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 업로드 중 오류가 발생했습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("매니저 추가 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /** Manager 업데이트 */
    @PutMapping("update/{managerNum}")
    public ResponseEntity<String> updateManager(
            @RequestBody Map<String, String> request,
            @PathVariable Long managerNum) {

        String permissionState = request.get("permissionState");
        String managerAddress = request.get("managerAddress");
        String managerPhone = request.get("managerPhone");

        int result = managerService.updateManager(managerNum, permissionState, managerAddress, managerPhone);

        if (result > 0) {
            return ResponseEntity.ok("Manager and Permission updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No manager was updated");
        }
    }

    // 의사 이름 불러오기
    @GetMapping("/doctorname")
    public ResponseEntity<?> findManagersBySubstring() {
        List<Map<String, Object>> managers = managerService.findManagersBySubstring();
        if (managers.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(managers);
    }

    // 메니저 ID로 이름찾기
    @GetMapping("/selectManagerIdWhereRHK/{selectedDepartment}")
    public ResponseEntity<?> selectManagerIdWhereRHK(@PathVariable("selectedDepartment") String selectedDepartment) {
        List<Map<String, Object>> managerName = managerService.selectManagerIdWhereRHK(selectedDepartment);
        return ResponseEntity.ok(managerName);
    }

    @GetMapping("/manager/role")
    public ResponseEntity<?> getPermissonRole(
            @RequestParam(name = "page") long page,
            @RequestParam(name = "permissionRole") String permissionRole) {
        try {
            Map<String, Object> result = new HashMap<>();

            long count = managerService.getTotalCount(); // 총 매니저 수 조회
            PageDTO pdto = PageDTO.builder()
                    .currentPage(page)
                    .pageSize(10L)
                    .totalRecords(count)
                    .totalPages((long) Math.ceil((double) count / 10)) // 올림 처리
                    .startIndex((page - 1) * 10) // 시작 인덱스 계산
                    .endIndex(Math.min(page * 10, count)) // 끝 인덱스 계산
                    .hasPrevPage(page > 1) // 이전 페이지 여부
                    .hasNextPage(page < Math.ceil((double) count / 10)) // 다음 페이지 여부
                    .build();

            result.put("PageDTO", pdto);
            result.put("list", managerService.selectPermissonRole(permissionRole, pdto));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("실패: " + e.getMessage());
        }
    }

}