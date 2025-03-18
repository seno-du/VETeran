package com.jjangtrio.veteran.ServerApplication.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jjangtrio.veteran.ServerApplication.dto.PageDTO;
import com.jjangtrio.veteran.ServerApplication.service.VaccineService;

@RestController
@RequestMapping("/api/vaccine")
public class VaccineController {

    @Autowired
    private VaccineService vaccineService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllVaccine(@RequestParam(name = "page") long page) {
        try {
            Map<String, Object> result = new HashMap<>();

            long count = vaccineService.totalCount(); // 총 매니저 수 조회
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

            System.out.println("count : " + count);
            result.put("PageDTO", pdto);
            result.put("list", vaccineService.findAllVaccine(pdto));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("실패: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchVaccine(@RequestParam(name = "vaccineNum") Long vaccineNum) {
        return ResponseEntity.ok(vaccineService.selectVaccine(vaccineNum));
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateVaccine(@RequestParam(name = "vaccineNum") Long vaccineNum,
            @RequestParam(name = "petNum") Long petNum,
            @RequestParam(name = "itemId") String itemId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long managerNum = Long.valueOf(auth.getPrincipal().toString());

        try {
            vaccineService.updateVaccine(vaccineNum, managerNum, petNum, itemId);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("실패: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<String> addVaccine(
            @RequestParam(name = "petNum") Long petNum, @RequestParam(name = "itemId") String itemId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long managerNum = Long.valueOf(auth.getPrincipal().toString());

        try {
            vaccineService.insertVaccine(managerNum, petNum, itemId);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("실패: " + e.getMessage());
        }
    }
}
