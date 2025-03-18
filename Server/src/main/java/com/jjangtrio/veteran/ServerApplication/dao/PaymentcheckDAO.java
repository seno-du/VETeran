package com.jjangtrio.veteran.ServerApplication.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import io.lettuce.core.dynamic.annotation.Param;

@Mapper
public interface PaymentcheckDAO {

    List<Map<String, Object>> selectList(@Param("pageSize") int pageSize, @Param("offset") int offset);

    int selectCount();
}
