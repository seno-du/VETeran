package com.jjangtrio.veteran.ServerApplication.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jjangtrio.veteran.ServerApplication.service.PaymentcheckService;

@RestController
@RequestMapping("/api/paymentcheck")
public class PaymentcheckController {

    @Autowired
    private PaymentcheckService paymentcheckService;

    @GetMapping
    public ResponseEntity<?> selectList(@RequestParam(value = "page", defaultValue = "1") int page) {

        int pageSize = 10;
        int totalCount = paymentcheckService.selectCount();
        totalCount += 1;
        List<Map<String, Object>> list = paymentcheckService.selectList(pageSize, page);

        HashMap<String, Object> map = new HashMap<>();
        map.put("totalCount", totalCount);
        map.put("pageSize", pageSize);
        map.put("currentPage", page);
        
        map.put("paymentCheck", list);

        return ResponseEntity.ok(map);
    }
}
