package com.jjangtrio.veteran.ServerApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentrequestDTO {

    private String paymentId; // 결제 고유 ID
    private String orderId; // 주문 ID (유니크)
    private String paymentMethod; // 결제 방법
    private String paymentStatus; // 결제 상태
    private Long amount; // 결제 금액

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Timestamp approvedAt; // 결제 승인 일시

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Timestamp requestedAt; // 결제 요청 일시
    private Long userNum;
    private Long reserveNum;

}
