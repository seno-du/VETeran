package com.jjangtrio.veteran.ServerApplication.controller;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import com.jjangtrio.veteran.ServerApplication.dto.HospitalLogDTO;
import com.jjangtrio.veteran.ServerApplication.dto.PageDTO;
import com.jjangtrio.veteran.ServerApplication.dto.UserDTO;
import com.jjangtrio.veteran.ServerApplication.service.HospitallogService;
import com.jjangtrio.veteran.ServerApplication.service.UserService;

@RestController
@RequestMapping("/api/hospital")
@CrossOrigin
public class HospitallogController {

    @Autowired
    private HospitallogService hospitalService;

    @Autowired
    private UserService userService;

    // 입력한 입원 방의 마지막 입원 환자만 조회
    @GetMapping("/single/{hospitalRoom}")
    public ResponseEntity<?> selectHospitalByRoomNum(@PathVariable Long hospitalRoom) {
        Map<String, Object> hospital = hospitalService.selectHospitalByRoomNum(hospitalRoom);
        return ResponseEntity.ok(hospital);
    }

    // 입원 기록의 날짜 리스트
    @GetMapping("/date")
    public ResponseEntity<?> getHospitalLogDateList() {
        List<String> dateList = hospitalService.getHospitalLogDateList();
        return ResponseEntity.ok(dateList);
    }

    // 현재 입원 중인 전체 환자 리스트 조회
    @GetMapping("/listOfActive")
    public ResponseEntity<?> getHospitalActiveList() {
        List<Map<String, Object>> hospitalData = hospitalService.getHospitalActiveList();
        return ResponseEntity.ok(hospitalData);
    }

    // 선택 날짜 입원 환자 조회
    @GetMapping("/date/{selectDay}")
    public ResponseEntity<?> getHospitalActiveListOfDay(@PathVariable String selectDay) {

        LocalDate date = LocalDate.parse(selectDay, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate newDate = date.plusDays(0);
        String updatedDate = newDate.toString();

        List<Map<String, Object>> hospitalData = hospitalService.getHospitalActiveListOfDay(updatedDate);
        return ResponseEntity.ok(hospitalData);
    }

    // 입원 환자 전체 조회
    @GetMapping("/list")
    public ResponseEntity<?> getAllHospitals() {
        try {
            List<Map<String, Object>> hospitalData = hospitalService.selectAllHospital();
            return ResponseEntity.ok(hospitalData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    // 입원 환자 추가 (DB 저장)
    @PostMapping("/add")
    public ResponseEntity<?> saveHospital(@RequestBody HospitalLogDTO hospitalDTO) {
        try {
            hospitalService.saveHospital(hospitalDTO);
            return ResponseEntity.ok("입원 스케줄 추가 완료");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("message", "이미 입원 중인 환자입니다."));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "입원 처리 중 오류 발생", "error", e.getMessage()));
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------
    // userNum으로 입원 내역 조회 and 입원 건수 확인 (김채린)
    // http://localhost:7124/back/api/hospital/mypage?page={page}
    @GetMapping("/mypage")
    public ResponseEntity<?> findHospitalLogByUserNum(@RequestParam(name = "page") long page) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userNum = Long.valueOf(auth.getPrincipal().toString());

            UserDTO user = userService.selectUserNum(userNum);
            if (user == null)
                return ResponseEntity.status(406).body("존재하지 않는 회원입니다.");

            long count = hospitalService.countHospitallLogByUserNum(userNum);

            Map<String, Object> result = new HashMap<>();

            PageDTO pdto = PageDTO.builder()
                    .currentPage(page)
                    .pageSize(5L)
                    .totalRecords(count)
                    .totalPages((long) (Math.ceil((double) count / 5)))
                    .startIndex((long) ((page - 1) * 5))
                    .endIndex((long) Math.min(page * 5, count))
                    .hasPrevPage(page > 1)
                    .hasNextPage(page < (int) Math.ceil((double) count / 5))
                    .build();

            result.put("PageDTO", pdto);
            result.put("list",
                    hospitalService.findHospitalLogByUserNum(userNum, pdto.getPageSize(), pdto.getStartIndex()));

            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body("error");
        }
    }

    // 퇴원 처리 (DB 업데이트)
    @GetMapping("/discharge/{hospitalRoom}")
    public ResponseEntity<?> hospitalStatus(@PathVariable("hospitalRoom") Long hospitalRoom) {
        try {
            Map<String, Object> hospital = hospitalService.selectHospitalByRoomNum(hospitalRoom);

            if (hospital != null && "활성".equals(hospital.get("hospitalStatus"))) {

                // 퇴원 처리
                hospitalService.hospitalStatus(hospitalRoom);

                Map<String, Object> updatedHospital = hospitalService.selectHospitalByRoomNum(hospitalRoom);
                String newStatus = (String) updatedHospital.get("hospitalStatus");
                return ResponseEntity.ok(Map.of(
                        "message", "퇴원 처리가 성공했습니다.",
                        "hospitalStatus", newStatus));
            }
            return ResponseEntity.status(406).body(Map.of(
                    "message", "이미 퇴원 처리된 입원실 입니다.",
                    "hospitalStatus", hospital.get("hospitalStatus")));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "message", "서비스 실행에 실패했습니다.",
                    "error", e.getMessage()));
        }
    }

}
