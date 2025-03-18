package com.jjangtrio.veteran.ServerApplication.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jjangtrio.veteran.ServerApplication.dto.PaymentrequestDTO;

import io.lettuce.core.dynamic.annotation.Param;

@Mapper
public interface PaymentrequestDAO {

    void insert(PaymentrequestDTO paymentrequestdto);

    void updateState(@Param("state") String state, @Param("paymentKey") String paymentKey,
            @Param("userNum") Long userNum, @Param("reserveNum") Long reserveNum);

    List<Map<String, Object>> selectPay(@Param("pageSize") int pageSize, @Param("offset") int offset);

    int countPay();
}
