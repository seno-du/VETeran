package com.jjangtrio.veteran.ServerApplication.service;

import java.sql.Timestamp;
// import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

import com.jjangtrio.veteran.ServerApplication.dao.ChartDAO;
import com.jjangtrio.veteran.ServerApplication.dao.HospitallogDAO;
import com.jjangtrio.veteran.ServerApplication.dto.ChartDTO;
import com.jjangtrio.veteran.ServerApplication.dto.HospitalLogDTO;

@Service
public class ChartService {

    @Autowired
    private ChartDAO chartDAO;

    @Autowired
    private HospitallogDAO hospitallogDAO; // 입원기록 DAO

    // 단일 차트 조회
    // public Map<String, Object> selectChart(Long chartNum) {
    //     return chartDAO.selectChart(chartNum);
    // }

    public Map<String, Object> selectChart(Long chartNum) {
        Map<String, Object> result = chartDAO.selectChart(chartNum);
    
        if (result == null) {
            return null;
        }
    
        // ✅ hospitalNum이 0이면 자동으로 입원 기록 생성
        if ((Long) result.get("hospitalNum") == 0) {
            HospitalLogDTO newLog = HospitalLogDTO.builder()
                    .chartNum(chartNum)
                    .hospitalRoom(1L) // 기본 입원실
                    .hospitalStartTime(new Timestamp(System.currentTimeMillis()))
                    .hospitalMemo("자동 생성")
                    .hospitalStatus("활성")
                    .build();
    
            hospitallogDAO.saveHospital(newLog);
            result.put("hospitalNum", newLog.getHospitalNum()); // ✅ 새 hospitalNum을 반영
        }
    
        return result;
    }
    

    // 전체 차트 조회
    public List<ChartDTO> selectAllChart() {
        List<ChartDTO> charts = chartDAO.selectAllChart();
        return charts;
    }

    // 당일 차트 저장
    public void saveChart(ChartDTO chartDTO) {
        if(chartDTO.getChartNum() == null) {
            chartDTO.setChartDate(new java.sql.Date(System.currentTimeMillis()));
        } else {
            // 수정 작업이라면 현재 날짜로 갱신하고 싶다면 아래처럼 처리
            chartDTO.setChartDate(new java.sql.Date(System.currentTimeMillis()));
        }
        chartDAO.saveChart(chartDTO);
    }

    public Long selectLatestChart(Long petNum) {
        return chartDAO.selectLatestChart(petNum);
    }

    // 당일 차트 수정
    public void updateChart(Map<String, Object> map) {
        chartDAO.updateChart(map);
    }

    // 최신 열람 차트 조회
    List<ChartDTO> selectNewChart() {
        return chartDAO.selectNewChart();
    }

    public List<Map<String, Object>> selectChartHistory(Long petNum) {
        return chartDAO.selectChartHistory(petNum);
    }

    // userNum로 차트 리스트 조회 (김채린)
    public List<Map<String, Object>> findChartByUserNum(Long userNum, Long size, Long start) {
        return chartDAO.findChartByUserNum(userNum, size, start);
    }
    
    // userNum으로 진료 기록 카운트 (김채린)
    public Long countChartByUserNum(Long userNum) {
         return chartDAO.countChartByUserNum(userNum); 
    }

    // 특정 chartNum의 모든 진료 날짜 조회
    public List<String> getChartDates(Long chartNum) {
        return chartDAO.getChartDates(chartNum);
    }

    // 특정 chartNum + chartDate의 진료 기록 조회
    public Map<String, Object> historyDetail(Long chartNum) {
        System.out.println("historyDetail");
        return chartDAO.historyDetail(chartNum);
    }
    

}
