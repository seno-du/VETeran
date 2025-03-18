package com.jjangtrio.veteran.ServerApplication.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jjangtrio.veteran.ServerApplication.dto.ItemhistoryDTO;

import io.lettuce.core.dynamic.annotation.Param;

@Mapper
public interface ItemhistoryDAO {

    void insertItemhistory(ItemhistoryDTO itemhistoryDTO);

    List<Map<String, Object>> selectAll(@Param("pageSize") int pageSize, @Param("offset") int offset);

    List<Map<String, Object>> selectItemId(String itemId);

    List<Map<String, Object>> selectItemName(String itemName);

    List<Map<String, Object>> selectStockByLocation(String locationName);

    Map<String, Object> selectRemainingStockById(String itemId);

    Long countItem();

    public Map<String, Object> findItemHistoryByChart(Long chartNum);

    List<Map<String, Object>> findBillingItems(Long chartNum);

    Long selectEndHistory();

}
