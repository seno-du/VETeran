package com.jjangtrio.veteran.ServerApplication.controller;

import java.util.Map;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jjangtrio.veteran.ServerApplication.config.AESUtil;
import com.jjangtrio.veteran.ServerApplication.config.UniqueIdUtil;
import com.jjangtrio.veteran.ServerApplication.dto.PaymentrequestDTO;
import com.jjangtrio.veteran.ServerApplication.dto.UserDTO;
import com.jjangtrio.veteran.ServerApplication.service.PaymentrequestService;
import com.jjangtrio.veteran.ServerApplication.service.UserService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/paymentrequest")
public class PaymentrequestController {

    @Autowired
    private PaymentrequestService paymentrequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private AESUtil aesUtil;

    @PostMapping
    public ResponseEntity<?> insert(@RequestBody PaymentrequestDTO dto) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userNum = Long.valueOf(auth.getPrincipal().toString());

        try {
            if (userNum == null)
                return ResponseEntity.badRequest().body("null token");

            if (dto.getReserveNum().equals(null))
                return ResponseEntity.badRequest().body("null reserveNum");

            dto.setUserNum(userNum);

            // orderId는 UUID로 생성
            String orderId = UniqueIdUtil.generateOrderId(); // UUID 생성
            dto.setOrderId(orderId);

            // paymentId는 UUID로 생성 후, AES로 암호화
            String paymentId = UniqueIdUtil.generatePaymentId();
            String encryptedPaymentId = aesUtil.encrypt(paymentId);

            dto.setPaymentId(encryptedPaymentId);
            dto.setPaymentStatus("준비됨");

            // 서비스 호출하여 결제 요청 등록
            paymentrequestService.insert(dto);

            UserDTO user = userService.selectUserNum(userNum);

            HashMap<String, Object> map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("userEmail", user.getUserEmail());
            map.put("userName", user.getUserName());
            map.put("userPhone", user.getUserPhone());

            return ResponseEntity.ok(map); // 성공 시 orderId 반환
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage()); // 에러 처리
        }
    }

    @PostMapping("/updateState")
    public ResponseEntity<?> updateState(@RequestBody Map<String, Object> map) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userNum = Long.valueOf(auth.getPrincipal().toString());

        try {
            if (userNum.equals(null))
                return ResponseEntity.badRequest().body("null token");

            String state = map.get("state").toString();
            String paymentKey = map.get("paymentKey").toString();

            Integer reserveNum1 = Integer.valueOf(map.get("reserveNum").toString());
            Long reserveNum = Long.valueOf(reserveNum1);
            paymentrequestService.updateState(state, paymentKey, userNum, reserveNum);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/paymentList")
    public ResponseEntity<?> selectPay(@RequestParam(value = "page", defaultValue = "1") int page) {

        try {
            int pageSize = 10;

            int totalCount = paymentrequestService.countPay();
            List<Map<String, Object>> list = paymentrequestService.selectPay(pageSize, page);
            HashMap<String, Object> map = new HashMap<>();

            map.put("pageSize", pageSize);
            map.put("totalCount", totalCount);
            map.put("length", list.size());
            map.put("page", page);
            map.put("paymentList", list);
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

}
