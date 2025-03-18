package com.jjangtrio.veteran.ServerApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.security.Timestamp;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentcheckDTO {

    private Long id; // 결제 체크 ID
    private Double mse; // MSE 값
    private Double normalAboveThreshold; // 정상 이상치 임계값을 초과한 비율
    private Double anomalyProbability; // 이상 확률
    private Boolean isAnomaly; // 이상 여부
    private String userIP; // 사용자 IP 주소
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Timestamp createdAt; // 생성 일시
    private Long userNum; // 사용자 번호
}