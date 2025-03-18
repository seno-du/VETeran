package com.jjangtrio.veteran.ServerApplication.controller;

import java.sql.Time;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jjangtrio.veteran.ServerApplication.dto.ItemhistoryDTO;
import com.jjangtrio.veteran.ServerApplication.dto.SupplierDTO;
import com.jjangtrio.veteran.ServerApplication.service.ItemhistoryService;
import com.jjangtrio.veteran.ServerApplication.service.SupplierService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/supplier")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private ItemhistoryService itemhistoryService;

    @PostMapping("/insert")
    public ResponseEntity<?> insertSupplier(@RequestBody SupplierDTO supplierDTO) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long managerNum = Long.valueOf(auth.getPrincipal().toString());

        try {
            if (supplierDTO == null) {
                return ResponseEntity.status(400).body("Invalid input data");
            }

            supplierDTO.setManagerNum(managerNum);

            String existingTransactionId = supplierService
                    .selectSupplierTransactionId(supplierDTO.getSupplierTransactionId());

            // 기존 거래 ID가 없거나 다른 경우
            if (existingTransactionId == null
                    || !existingTransactionId.equals(supplierDTO.getSupplierTransactionId())) {

                supplierService.insertSupplier(supplierDTO);

                String itemId = supplierDTO.getItemId();
                Long supplierProductQuantity = supplierDTO.getSupplierProductQuantity();

                ItemhistoryDTO itemhistoryDTO = new ItemhistoryDTO();
                itemhistoryDTO.setItemId(itemId);
                itemhistoryDTO.setLocationId(1L);
                itemhistoryDTO.setHistoryQuantity(supplierProductQuantity);
                itemhistoryDTO.setTransactionType("입고");
                itemhistoryDTO.setTransactionDate(new Timestamp(System.currentTimeMillis()));
                itemhistoryService.insertItemhistory(itemhistoryDTO);

                return ResponseEntity.ok().body("success");
            } else {
                return ResponseEntity.ok().body("isOrder");
            }
        } catch (Exception e) {
            e.printStackTrace(); // 예외 출력
            return ResponseEntity.status(500).body("error: " + e.getMessage());
        }
    }

}
