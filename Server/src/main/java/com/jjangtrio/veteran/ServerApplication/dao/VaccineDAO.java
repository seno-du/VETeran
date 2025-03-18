package com.jjangtrio.veteran.ServerApplication.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VaccineDAO {

    List<Map<String, Object>> findAllVaccine(@Param("start") long start, @Param("size") long size);

    long totalCount();

    Map<String, Object> selectVaccine(@Param("vaccineNum") Long vaccineNum);

    void updateVaccine(@Param("vaccineNum") Long vaccineNum
            , @Param("managerNum") Long managerNum
            , @Param("petNum") Long petNum
            , @Param("itemId") String itemId);

    void insertVaccine(@Param("managerNum") Long managerNum,
            @Param("petNum") Long petNum,
            @Param("itemId") String itemId);
}
