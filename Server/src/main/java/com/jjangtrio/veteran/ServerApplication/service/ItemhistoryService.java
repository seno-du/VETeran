package com.jjangtrio.veteran.ServerApplication.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jjangtrio.veteran.ServerApplication.dao.ChartDAO;
import com.jjangtrio.veteran.ServerApplication.dao.ItemhistoryDAO;
import com.jjangtrio.veteran.ServerApplication.dto.ItemhistoryDTO;

@Service
public class ItemhistoryService {

    @Autowired
    private ItemhistoryDAO itemhistoryDAO;

    // 효진쓰 가져옴
    @Autowired
    private ChartDAO chartDAO;

    public void insertItemhistory(ItemhistoryDTO itemhistoryDTO) {
        itemhistoryDAO.insertItemhistory(itemhistoryDTO);
    }

    public List<Map<String, Object>> selectAll(int pageSize, int pageNum) {
        if (pageNum < 1)
            pageNum = 1; // 최소 1페이지로 보정
        int offset = (pageNum - 1) * pageSize;
        return itemhistoryDAO.selectAll(pageSize, offset);
    }

    public Map<String, Object> selectItemId(String itemId) {
        // 데이터 조회
        List<Map<String, Object>> stockData = itemhistoryDAO.selectItemId(itemId);

        // 전체 재고 계산
        int totalRemainingStock = 0;
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> locationStocks = new ArrayList<>();

        for (Map<String, Object> data : stockData) {
            // "locationRemainingStock" 값이 BigDecimal로 반환될 수 있기 때문에 BigDecimal을 Long으로 변환
            BigDecimal locationRemainingStock = (BigDecimal) data.get("locationRemainingStock");
            if (locationRemainingStock != null) {
                totalRemainingStock += locationRemainingStock.intValue();
            } else {
                // locationRemainingStock가 null일 경우 0으로 처리
                totalRemainingStock += 0;
            }

            // 구역별 재고 정보 추가
            locationStocks.add(data);
        }

        // 결과 구성
        result.put("totalRemainingStock", totalRemainingStock);
        result.put("locations", locationStocks);
        return result;
    }

    public Map<String, Object> selectItemName(String itemName) {
        // 데이터 조회
        List<Map<String, Object>> stockData = itemhistoryDAO.selectItemName(itemName);

        // 전체 재고 계산
        int totalRemainingStock = 0;
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> locationStocks = new ArrayList<>();

        for (Map<String, Object> data : stockData) {
            // Null 체크 후 계산
            BigDecimal locationRemainingStock = (BigDecimal) data.get("locationRemainingStock");
            if (locationRemainingStock != null) {
                totalRemainingStock += locationRemainingStock.intValue();
            } else {
                // locationRemainingStock가 null일 경우 0으로 처리
                totalRemainingStock += 0;
            }

            // 구역별 재고 정보 추가
            locationStocks.add(data);
        }

        // 결과 구성
        result.put("totalRemainingStock", totalRemainingStock);
        result.put("locations", locationStocks);
        return result;
    }

    public List<Map<String, Object>> selectStockByLocation(String locationName) {
        return itemhistoryDAO.selectStockByLocation(locationName);
    }

    public Long selectRemainingStockById(String itemId) {
        // 데이터베이스에서 결과를 Map으로 반환 받음
        Map<String, Object> result = itemhistoryDAO.selectRemainingStockById(itemId);

        // 첫 번째 결과를 확인하여 remainingStock을 BigDecimal로 가져옴
        BigDecimal remainingStock = (BigDecimal) result.get("remainingStock");

        // BigDecimal을 Long으로 변환
        if (remainingStock != null) {
            return remainingStock.longValue();
        } else {
            return 0L; // 만약 값이 없다면 0을 반환
        }
    }

    public Long countItem() {
        return itemhistoryDAO.countItem();
    }

    public Map<String, Object> getItemHistoryByChart(Long chartNum) {
        return itemhistoryDAO.findItemHistoryByChart(chartNum);
    }

    public List<Map<String, Object>> getBillingItems(Long chartNum) {
        return itemhistoryDAO.findBillingItems(chartNum);
    }

    public Long selectEndHistory() {
        return itemhistoryDAO.selectEndHistory();
    }
}
