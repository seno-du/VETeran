package com.jjangtrio.veteran.ServerApplication.service;

import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jjangtrio.veteran.ServerApplication.dao.HospitallogDAO;
import com.jjangtrio.veteran.ServerApplication.dto.HospitalLogDTO;

@Service
public class HospitallogService {

    @Autowired
    private HospitallogDAO hospitalDAO;

   // 입력한 입원 방의 마지막 입원 환자만 조회--
   public Map<String, Object> selectHospitalByRoomNum(Long hospitalRoom) {
        return hospitalDAO.selectHospitalByRoomNum(hospitalRoom);
    }

    // 입원 기록의 날짜 리스트--
    public List<String> getHospitalLogDateList(){

        List<Timestamp> DateList = hospitalDAO.getHospitalLogDateList();

        List<String> dateList = new ArrayList<>();

        for (Timestamp chatAiDate : DateList) {
            Date date = new Date(chatAiDate.getTime());
            System.out.println("Date: " + date);
            dateList.add(date.toString());
        }

        return dateList;
    }

    // 현재 입원 중인 전체 환자 리스트 조회 
    public List<Map<String, Object>> getHospitalActiveList() {
        return hospitalDAO.getHospitalActiveList();
    }

    // 선택 날짜 입원 환자 조회 --
    public List<Map<String, Object>> getHospitalActiveListOfDay(String selectday) {
        return hospitalDAO.getHospitalActiveListOfDay(selectday);
    }

    // 입원 환자 전체 조회
    public List<Map<String, Object>> selectAllHospital() {
        return hospitalDAO.selectAllHospital();
    }

    // 입원 환자 추가 (DB 저장)
    @Transactional
    public void saveHospital(HospitalLogDTO hospitalDTO) {
        // 이미 입원 중인지 chartNum 기준으로 확인
        Long existingHospital = hospitalDAO.checkHospital(hospitalDTO.getChartNum());
        System.out.println("checkHospital() 결과: " + existingHospital);

        if (existingHospital != null && existingHospital > 0) {
            throw new IllegalStateException("이미 입원 중인 환자입니다.");
        }

        // 중복이 아니라면 입원 처리 진행
        hospitalDAO.saveHospital(hospitalDTO);
    }
    
    // userNum으로 입원 내역 조회 (김채린)--
    public List<Map<String, Object>> findHospitalLogByUserNum (Long userNum, Long size, Long start){
        return hospitalDAO.findHospitalLogByUserNum(userNum, size, start);
    }

    // userNum으로 입원 내역 카운트 (김채린)--
    public Long countHospitallLogByUserNum(Long userNum) {
        return hospitalDAO.countHospitallLogByUserNum(userNum);
    }

    // 퇴원 처리--
    @Transactional
    public void hospitalStatus(Long hospitalRoom) {
        hospitalDAO.hospitalStatus(hospitalRoom);
    }
    

}
