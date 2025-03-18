package com.jjangtrio.veteran.ServerApplication.dao;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jjangtrio.veteran.ServerApplication.dto.ReserveDTO;

@Mapper
public interface ReserveDAO {

        List<Map<String, Object>> findAllReserve(@Param("start") long start, @Param("size") long size);

        long totalCount();

        void insertReserve(ReserveDTO reserve);

        // userNum으로 예약 내역 조회 (김채린)
        List<Map<String, Object>> findReserveByUserNum(@Param("userNum") Long userNum, @Param("size") long size,
                        @Param("start") long start);

        // reserveNum으로 예약 조회 (김채린)
        ReserveDTO findDetailByreserveNum(@Param("reserveNum") Long reserveNum);

        // reserveNum으로 예약 취소 (김채린)
        void unReservation(Long reserveNum);

        // userNum으로 예약 내역 카운트 (김채린)
        Long countReserveByUserNum(Long reserveNum);

        // 전체 예약 조회
        ReserveDTO findAllReservations(Date reserveDate);

        // 예약 현황 페이징 처리
        List<ReserveDTO> pageReserveByDate(Map<String, Object> params);

        // 예약 대기 현황 페이징 처리
        int countPendingReserve();
        List<ReserveDTO> findPendingReserve(Map<String, Object> params);

        // 예약 승인
        int updateReserveStatusToComplete(@Param("reserveNum") List<Long> reserveNum,
                        @Param("reserveStatus") String reserveStatus);

        void updateStateWhereReserveNum(@Param("reserveStatus") String reserveStatus,
                        @Param("reserveNum") Long reserveNum);

        // 금일 예약 갯수 갖고오기
        Long countReserveByDate(@Param("targetDate") String targetDate);

        // 금주 예약 갯수 갖고오기
        Long countReserveByWeek(@Param("targetDate") String targetDate);

        // 월간 예약 갯수 갖고오기
        Long countReserveByMonth(@Param("targetMonth") String targetDate);

        // pet정보로 예약 가져오기
        List<ReserveDTO> findReserveByPetNum(@Param("petNum") Long petNum);

        // 마지막 reserveNum 가져오기
        Long limitReserveNum();
}
