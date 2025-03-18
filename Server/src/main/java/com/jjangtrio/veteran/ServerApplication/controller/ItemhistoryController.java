package com.jjangtrio.veteran.ServerApplication.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jjangtrio.veteran.ServerApplication.dto.ItemhistoryDTO;
import com.jjangtrio.veteran.ServerApplication.service.ItemhistoryService;

@RestController
@RequestMapping("/api/itemhistory")
public class ItemhistoryController {

    @Autowired
    private ItemhistoryService itemhistoryService;

    @PostMapping("/insert")
    @Transactional
    public ResponseEntity<?> insertItemhistory(@RequestBody List<ItemhistoryDTO> itemhistoryDTOList) {
        List<Long> historyNums = new ArrayList<>();
        List<ItemhistoryDTO> failedItems = new ArrayList<>();

        for (ItemhistoryDTO itemhistoryDTO : itemhistoryDTOList) {
            Long inputQuantity = itemhistoryDTO.getHistoryQuantity();
            Long historyQuantity = itemhistoryService.selectRemainingStockById(itemhistoryDTO.getItemId());

            // 출고 시 수량 부족 검사
            if (itemhistoryDTO.getTransactionType().equals("출고") && historyQuantity - inputQuantity < 0) {
                failedItems.add(itemhistoryDTO);
                continue; // 실패한 항목은 저장하지 않고 다음 루프로 이동
            }

            // 데이터 삽입
            itemhistoryService.insertItemhistory(itemhistoryDTO);
            historyNums.add(itemhistoryService.selectEndHistory());
        }

        // 일부 실패한 항목이 있을 경우, 실패한 항목 목록을 반환
        if (!failedItems.isEmpty()) {
            HashMap<String, Object> response = new HashMap<>();
            response.put("message", "일부 항목 저장 완료, 일부 항목 실패");
            response.put("historyNum", historyNums);
            response.put("failedItems", failedItems);
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
        }

        // 모든 항목이 성공한 경우
        HashMap<String, Object> response = new HashMap<>();
        response.put("historyNum", historyNums);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/selectall/{pageNum}")
    public ResponseEntity<?> selectAll(@PathVariable("pageNum") int pageNum) {
        int pageSize = 10;
        Long totalSize = itemhistoryService.countItem();
        System.out.println("totalSize: " + totalSize);
        List<Map<String, Object>> items = itemhistoryService.selectAll(pageSize, pageNum);

        // 페이지 정보를 담을 맵
        HashMap<String, Object> map = new HashMap<>();

        map.put("pageSize", pageSize);
        map.put("totalSize", totalSize);
        map.put("items", items); // 실제 아이템 리스트
        return ResponseEntity.ok(map);
    }

    @GetMapping("/itemid/{itemId}")
    public ResponseEntity<?> selectItemId(@PathVariable("itemId") String itemId) {
        Map<String, Object> result = itemhistoryService.selectItemId(itemId);
        if (result.equals(null)) {
            return ResponseEntity.ok().body("다시 입력해주세요.");
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/itemname/{itemName}")
    public ResponseEntity<?> selectItemName(@PathVariable("itemName") String itemName) {
        Map<String, Object> result = itemhistoryService.selectItemName(itemName);
        if (result.equals(null)) {
            return ResponseEntity.ok().body("다시 입력해주세요.");
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/chart/{chartNum}")
    public ResponseEntity<?> getItemHistoryByChart(@PathVariable("chartNum") Long chartNum) {
        Map<String, Object> historyList = itemhistoryService.getItemHistoryByChart(chartNum);

        if (historyList == null || historyList.isEmpty()) {
            return ResponseEntity.badRequest().body("해당 차트의 재고 이력이 없습니다.");
        }

        return ResponseEntity.ok(historyList);
    }

    @GetMapping("/billing")
    public List<Map<String, Object>> getBillingItems(@RequestParam(name = "chartNum") Long chartNum) {
        return itemhistoryService.getBillingItems(chartNum);
    }
}
