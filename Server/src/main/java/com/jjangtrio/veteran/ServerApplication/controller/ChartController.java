package com.jjangtrio.veteran.ServerApplication.controller;

import com.jjangtrio.veteran.ServerApplication.dto.ChartDTO;
import com.jjangtrio.veteran.ServerApplication.dto.PageDTO;
import com.jjangtrio.veteran.ServerApplication.service.ChartService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chart")
@CrossOrigin
public class ChartController {

    @Autowired
    private ChartService chartService;

    // 단일 차트 조회
    @GetMapping("/{chartNum}")
    public ResponseEntity<?> selectChart(@PathVariable Long chartNum) {
        try {
            Map<String, Object> currentChart = chartService.selectChart(chartNum);
            List<Map<String, Object>> chartHistory = chartService
                    .selectChartHistory(((Integer) currentChart.get("petNum")).longValue());

            Map<String, Object> response = new HashMap<>();
            response.put("currentChart", currentChart);
            response.put("chartHistory", chartHistory);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body("알 수 없는 오류 발생");
        }
    }

    // 당일 차트 저장 API
    @PostMapping("/save")
    public ResponseEntity<?> saveChart(@RequestBody ChartDTO chartDTO) {
        try {
            // chartService를 통해 ChartDTO를 저장합니다.
            chartService.saveChart(chartDTO);
            System.out.println("진료 저장 완료 =>" + chartDTO.toString());
            // 저장 성공 시 "저장 완료" 메시지를 반환합니다.
            return ResponseEntity.ok("저장 완료");
        } catch (Exception e) {
            // 예외 발생 시 상태코드 500과 함께 에러 메시지를 반환합니다.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("저장 실패: " + e.getMessage());
        }
    }

    // 특정 차트의 모든 진료 날짜 조회 API
    @GetMapping("/{chartNum}/dates")
    public ResponseEntity<?> getChartDates(@PathVariable Long chartNum) {
        try {
            List<String> dates = chartService.getChartDates(chartNum);
            System.out.println(" 조회된 진료일자 => " + chartNum);
            return ResponseEntity.ok(dates);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ 조회 실패: " + e.getMessage());
        }
    }

    // 특정 차트의 특정 날짜 진료내역 조회 API
    @GetMapping("/detail/{chartNum}")
    public ResponseEntity<?> chartHistoryDetail(@PathVariable Long chartNum) {
        try {
            System.out.println(" 컨트롤러 요청된 chartNum: " + chartNum);

            Map<String, Object> result = chartService.historyDetail(chartNum);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ 조회 실패: " + e.getMessage());
        }
    }

    // 최신 차트 조회
    @GetMapping("/getLatestChart")
    public ResponseEntity<?> getLatestChart(@RequestParam Long petNum) {
        try {
            Long chartNum = chartService.selectLatestChart(petNum);
            if (chartNum == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("chartNum", "NOT_FOUND");
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.ok(chartNum);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body("알 수 없는 오류 발생");
        }
    }

    @PostMapping
    public ResponseEntity<?> updateChart(@RequestBody Map<String, Object> map) {
        try {
            System.out.println("Subjective:" + map.get("subjective"));
            System.out.println("chartNum:" + map.get("chartNum"));
            for (String k : map.keySet()) {
                System.out.println(k + " -> " + map.get(k));
            }
            chartService.updateChart(map);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body("알 수 없는 오류 발생");
        }
    }

    // ----------------------------------------------------------------------------------------------------------------
    // userNum으로 진료 내역 조회 and 진료 건수 확인
    // http://localhost:7124/back/api/chart/mypage?page={page}
    @GetMapping("/mypage")
    public ResponseEntity<?> findHospitalLogByUserNum(@RequestParam(name = "page") long page) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userNum = Long.valueOf(auth.getPrincipal().toString());

            Map<String, Object> result = new HashMap<>();

            long count = chartService.countChartByUserNum(userNum);

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
            result.put("list", chartService.findChartByUserNum(userNum, pdto.getPageSize(), pdto.getStartIndex()));

            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body("error");
        }
    }

    // 전체 차트 조회
    @GetMapping("/all")
    public List<ChartDTO> selectAllChart() {
        List<ChartDTO> chartList = chartService.selectAllChart();
        System.out.println("컨트롤러 전체 조회 결과: " + chartList);
        return chartList;
    }

}
