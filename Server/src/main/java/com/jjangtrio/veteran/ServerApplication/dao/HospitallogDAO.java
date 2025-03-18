package com.jjangtrio.veteran.ServerApplication.dao;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jjangtrio.veteran.ServerApplication.dto.HospitalLogDTO;

@Mapper
public interface HospitallogDAO {


    // 입력한 입원 방의 마지막 입원 환자만 조회
    Map<String, Object> selectHospitalByRoomNum(@Param("hospitalRoom") Long hospitalRoom);

    // 입원 기록의 날짜 리스트
    public List<Timestamp> getHospitalLogDateList();

    // 현재 입원 중인 전체 환자 리스트 조회 
    List<Map<String, Object>> getHospitalActiveList();

    // 선택 날짜 입원 환자 조회 
    List<Map<String, Object>> getHospitalActiveListOfDay(@Param("selectday") String selectday);

    // 입원 환자 전체 조회
    List<Map<String, Object>> selectAllHospital();

    // 입원 환자 추가
    void saveHospital(HospitalLogDTO hospitalDTO);

    // userNum으로 입원 내역 조회 (김채린)
    List<Map<String, Object>> findHospitalLogByUserNum(@Param("userNum") Long userNum, @Param("size") long size,
            @Param("start") long start);

    // userNum으로 입원 내역 카운트 (김채린)
    Long countHospitallLogByUserNum(Long userNum);

    // 퇴원시키기
    void hospitalStatus(@Param("hospitalRoom") Long hospitalRoom);

    // 중복 입원 처리
    Long checkHospital(Long petNum);

}
