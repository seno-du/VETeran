package com.jjangtrio.veteran.ServerApplication.controller;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.jjangtrio.veteran.ServerApplication.dto.PageDTO;
import com.jjangtrio.veteran.ServerApplication.dto.ReserveDTO;
import com.jjangtrio.veteran.ServerApplication.dto.UserDTO;
import com.jjangtrio.veteran.ServerApplication.service.ReserveService;
import com.jjangtrio.veteran.ServerApplication.service.UserService;

@RestController
@RequestMapping("/api/reserve")
public class ReserveController {

    @Autowired
    private ReserveService reserveService;

    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public ResponseEntity<?> findAllReserves(@RequestParam(name = "page") long page) {
        try {
            HashMap<String, Object> list = new HashMap<>();

            long count = reserveService.totalCount();
            PageDTO pdto = PageDTO.builder()
                    .currentPage(page)
                    .pageSize(4L)
                    .totalRecords(count)
                    .totalPages((long) (Math.ceil((double) count / 4)))
                    .startIndex((long) ((page - 1) * 4 + 1))
                    .endIndex((long) Math.min(page * 4, count))
                    .hasPrevPage(page > 1) // 이전 페이지 여부
                    .hasNextPage(page < (int) Math.ceil((double) count / 4))
                    .build();
            list.put("PageDTO", pdto);
            list.put("list", reserveService.findAllReserve(pdto));

            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("실패: " + e.getMessage());
        }

    }

    private Long convertToLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Long 변환 실패: " + obj);
            }
        }
        throw new IllegalArgumentException("지원되지 않는 데이터 타입: " + obj.getClass().getName());
    }

    @PostMapping("/reservation")
    public ResponseEntity<?> addPetReserve(@RequestBody Map<String, Object> map) {
        try {
            // 숫자 변환 (Integer, Double 대비)
            String petNum1 = map.get("petNum").toString();
            String managerNum1 = map.get("managerNum").toString();

            Long petNum = Long.valueOf(petNum1);
            Long managerNum = Long.valueOf((managerNum1));

            // 날짜 변환 (ISO 8601 → Timestamp)
            String reserveDateStr = map.get("reserveDate").toString();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime localDateTime = LocalDateTime.parse(reserveDateStr, formatter);
            Timestamp reserveDate = Timestamp.valueOf(localDateTime);

            // 기본값 처리
            String reserveNotice = map.getOrDefault("reserveNotice", "").toString();

            // DTO 생성 및 저장
            ReserveDTO reserve = new ReserveDTO();
            reserve.setPetNum(petNum);
            reserve.setManagerNum(managerNum);
            reserve.setReserveDate(reserveDate);
            reserve.setReserveNotice(reserveNotice);

            reserveService.insertReserve(reserve);

            Long reserveNum = reserveService.limitReserveNum();

            HashMap<String, Object> result = new HashMap<>();
            result.put("reserveNum", reserveNum);

            return ResponseEntity.ok(result);

        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘못된 날짜 형식: " + e.getMessage());
        } catch (ClassCastException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("숫자 변환 오류: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("예외 발생: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------
    // userNum으로 예약 내역 조회 and 예약 건수 확인
    // http://localhost:7124/back/api/reserve/mypage?page={page}
    @GetMapping("/mypage")
    public ResponseEntity<?> findHospitalLogByUserNum(@RequestParam(name = "page") long page) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userNum = Long.valueOf(auth.getPrincipal().toString());

            if (userService.selectUserNum(userNum) == null)
                return ResponseEntity.status(406).body("존재하지 않는 회원입니다.");

            long count = reserveService.countReserveByUserNum(userNum);

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
            result.put("list", reserveService.findReserveByUserNum(userNum, pdto.getPageSize(), pdto.getStartIndex()));

            System.out.println(reserveService.findReserveByUserNum(userNum, pdto.getPageSize(), pdto.getStartIndex()));
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body("error");
        }
    }

    // 예약 취소
    //// http://localhost:7124/back/api/reserve/mypage/upreservataion/{reserveNum}/{userNum}
    @PostMapping("/mypage/upreservataion/{reserveNum}")
    public ResponseEntity<?> unReservation(@PathVariable("reserveNum") Long reserveNum,
            @RequestBody Map<String, Object> map) {
        try {

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userNum = Long.valueOf(auth.getPrincipal().toString());

            ReserveDTO reserve = reserveService.findDetailByreserveNum(reserveNum);
            UserDTO user = userService.selectUserNum(userNum);
            if (user != null && user.getUserStatus().equals("활성")) {
                map.put("userPwd", user.getUserPwd());
                if (userService.verifyPwd(map))
                    reserveService.unReservation(reserve.getReserveNum());
                return ResponseEntity.ok().body("예약 취소가 완료되었습니다.");
            }
            return ResponseEntity.status(406).body("서비스를 지원할 수 없습니다.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body("error");
        }
    }

    // 매니저 예약 리스트 조회
    @GetMapping("/all/{pageNum}")
    public ResponseEntity<?> findAllReservations(
            @RequestParam("reserveDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate reserveDate,
            @PathVariable("pageNum") Integer pageNum,
            @RequestParam(name = "pageSize", defaultValue = "8") int pageSize) {
        try {
            List<ReserveDTO> reservations = reserveService.getPagedReservationsByDate(reserveDate, pageNum, pageSize);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(500).build();
        }
    }

    // 매니저 예약 대기 리스트 조회
    @GetMapping("/pendinglist/{pageNum}")
    public ResponseEntity<?> findPendingReserve(
            @PathVariable("pageNum") Integer pageNum,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {

        Map<String, Object> result = reserveService.findPendingReserve(pageNum, pageSize);
        return ResponseEntity.ok(result); // totalCount 포함해서 응답
    }

    // 예약 승인
    @PutMapping("/reserveresponse")
    public ResponseEntity<?> updateReserveStatus(@RequestBody Map<String, Object> request) {
        // 요청에서 reserveNum이 있는지 확인
        try {
            if (!request.containsKey("reserveNum") || request.get("reserveNum") == null) {
                return ResponseEntity.ok("예약 정보를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(500).build();
        }

        List<?> rawReserveNums = (List<?>) request.get("reserveNum");

        // reserveNum이 비어있다면 에러 메시지 반환
        if (rawReserveNums.isEmpty()) {
            return ResponseEntity.ok("예약 정보를 찾을 수 없습니다.");
        }

        // reserveNum을 List<Long>으로 변환
        List<Long> reserveNum = rawReserveNums.stream()
                .map(num -> Long.valueOf(num.toString()))
                .toList();

        // reserveStatus를 String으로 변환
        String reserveStatus = request.get("reserveStatus").toString();

        reserveService.updateReserveStatusToComplete(reserveNum, reserveStatus);
        return ResponseEntity.ok("예약 상태가 성공적으로 변경되었습니다.");
    }

    @PostMapping("/updateState")
    public ResponseEntity<?> postMethodName(@RequestBody Map<String, Object> map) {

        String reserveStatus = map.get("reserveStatus").toString();
        Integer reserveNum1 = Integer.valueOf(map.get("reserveNum").toString());
        Long reserveNum = Long.valueOf(reserveNum1);

        reserveService.updateStateWhereReserveNum(reserveStatus, reserveNum);

        return ResponseEntity.ok().build();
    }

    // 예약 실시간 전송
    @SendTo("/topic/reserve")
    public ReserveDTO sendReserve(ReserveDTO reserve) {
        reserve.setReserveStatus("대기");
        reserveService.insertReserve(reserve);
        return reserve;
    }

    // 금일 예약 갯수 갖고오기
    @GetMapping("/count/daily")
    public ResponseEntity<?> getReserveCountByDate(@RequestParam("targetDate") String targetDate) {
        try {
            Long count = reserveService.getReserveCountByDate(targetDate);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(500).build();
        }
    }

    // 금주 예약 갯수 갖고오기
    @GetMapping("/count/weekly")
    public ResponseEntity<?> getReserveCountByWeek(@RequestParam("targetDate") String targetDate) {
        Long count = reserveService.getReserveCountByWeek(targetDate);
        return ResponseEntity.ok(count);
    }

    // 이번 달 예약 갯수 갖고오기
    @GetMapping("/count/month")
    public ResponseEntity<?> getReserveCountByMonth(@RequestParam("targetDate") String targetDate) {
        String targetMonth = targetDate.substring(0, 7); // YYYY-MM 포맷 변환
        Long count = reserveService.getReserveCountByMonth(targetMonth);
        return ResponseEntity.ok(count);
    }

    // 특정 petNum의 예약 리스트 조회 API
    @GetMapping("/pet/{petNum}")
    public ResponseEntity<?> findReserveByPetNum(@PathVariable("petNum") Long petNum) {
        try {
            List<ReserveDTO> reservations = reserveService.findReserveByPetNum(petNum);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("예약 조회 실패: " + e.getMessage());
        }
    }
}