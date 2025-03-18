package com.jjangtrio.veteran.ServerApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

import org.apache.ibatis.type.Alias;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Alias("supplierdto")
public class SupplierDTO {
    private Long supplierNum;
    private String supplierName;
    private String supplierCode;
    private String supplierEmployee;
    private Long managerNum;
    private String supplierTransactionId;
    private String itemId;
    private Double supplierProductWeight;
    private Long supplierProductQuantity;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date supplierExpirationDate;
}
