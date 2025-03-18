package com.jjangtrio.veteran.ServerApplication.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.mybatis.spring.annotation.MapperScan;

import com.jjangtrio.veteran.ServerApplication.dto.ChartDTO;

@Mapper
@MapperScan
public interface ChartDAO {

    // 단일 차트 조회
    Map<String, Object> selectChart(@Param("chartNum") Long chartNum);

    // 당일 차트 수정
    void updateChart(Map<String, Object> map);

    // 단일 차트 최신 조회
    Long selectLatestChart(Long petNum);

    // 전체 차트 조회
    List<ChartDTO> selectAllChart();

    // 당일 차트 저장
    void saveChart(ChartDTO chartDTO);

    // 최신 열람 차트 조회
    List<ChartDTO> selectNewChart();

    // managerNum 기반으로 차트 로그 목록 조회
    // List<Chartlog> viewChart(@Param("managerNum") Long managerNum);

    List<Map<String, Object>> selectChartHistory(Long chartNum);

    // 해당 chartNum 안에 진료내역 조회
    List<String> getChartDates(@Param("chartNum") Long chartNum);

    // 단일 진료내역 디테일
    Map<String, Object> historyDetail(@Param("chartNum") Long chartNum);

    // userNum로 차트 리스트 조회 (김채린)
    List<Map<String, Object>> findChartByUserNum(@Param("userNum")Long userNum, @Param("size") long size, @Param("start") long start);

    // userNum으로 진료 기록 카운트 (김채린)
    Long countChartByUserNum(Long userNum);

    
}
