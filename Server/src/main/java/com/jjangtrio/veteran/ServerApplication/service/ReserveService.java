package com.jjangtrio.veteran.ServerApplication.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jjangtrio.veteran.ServerApplication.dao.ReserveDAO;
import com.jjangtrio.veteran.ServerApplication.dto.PageDTO;
import com.jjangtrio.veteran.ServerApplication.dto.ReserveDTO;

@Service
public class ReserveService {

    @Autowired
    private ReserveDAO reserveDAO;

    public List<Map<String, Object>> findAllReserve(PageDTO pageDTO) {
        return reserveDAO.findAllReserve(pageDTO.getStartIndex(), pageDTO.getPageSize());
    }

    public long totalCount() {
        return reserveDAO.totalCount();
    }

    public void insertReserve(ReserveDTO reserve) {
        reserveDAO.insertReserve(reserve);
    }

    // userNum으로 예약 내역 조회 (김채린)
    public List<Map<String, Object>> findReserveByUserNum(Long userNum, Long size, Long start) {
        return reserveDAO.findReserveByUserNum(userNum, size, start);
    }

    // reserveNum으로 예약 조회 (김채린)
    public ReserveDTO findDetailByreserveNum(Long reserveNum) {
        return reserveDAO.findDetailByreserveNum(reserveNum);
    }

    // reserveNum으로 예약 취소 (김채린)
    public void unReservation(Long reserveNum) {
        reserveDAO.unReservation(reserveNum);
    }

    // userNum으로 예약 내역 카운트 (김채린)
    public Long countReserveByUserNum(Long reserveNum) {
        return reserveDAO.countReserveByUserNum(reserveNum);

    }

    // 예약 조회
    public List<ReserveDTO> getPagedReservationsByDate(LocalDate reserveDate, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return reserveDAO.pageReserveByDate(Map.of(
                "reserveDate", reserveDate.toString(), // 🔥 String으로 변환
                "pageSize", pageSize,
                "offset", offset));
    }

    // 예약 대기 조회
   public Map<String, Object> findPendingReserve(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize; // 페이지네이션을 위한 offset 계산
        List<ReserveDTO> reservations = reserveDAO.findPendingReserve(Map.of(
                "pageSize", pageSize,
                "offset", offset));
        int totalCount = reserveDAO.countPendingReserve(); // 전체 개수 조회

        // 🔥 데이터를 Map으로 반환
        Map<String, Object> response = new HashMap<>();
        response.put("reservations", reservations);
        response.put("totalCount", totalCount);

        return response;
    }

    // 예약 승인
    public void updateReserveStatusToComplete(List<Long> reserveNum, String reserveStatus) {
        if (!reserveStatus.equals("완료") && !reserveStatus.equals("취소")) {
            throw new IllegalArgumentException("예약 상태는 '완료' 또는 '취소'만 가능합니다.");
        }

        int updatedRows = reserveDAO.updateReserveStatusToComplete(reserveNum, reserveStatus);
        if (updatedRows == 0) {
            throw new IllegalStateException("예약 상태 변경 실패: 해당 예약이 존재하지 않거나 이미 변경되었습니다.");
        }
    }

    // 예약 상태변경
    public void updateStateWhereReserveNum(String reserveStatus, Long reserveNum) {
        reserveDAO.updateStateWhereReserveNum(reserveStatus, reserveNum);
    }

    // 금일 예약 갯수 갖고오기
    public Long getReserveCountByDate(String targetDate) {
        return reserveDAO.countReserveByDate(targetDate);
    }

    // 금주 예약 갯수 갖고오기
    public Long getReserveCountByWeek(String targetDate) {
        return reserveDAO.countReserveByWeek(targetDate);
    }

    // 이번 달 예약 갯수 갖고오기
    public Long getReserveCountByMonth(String targetDate) {
        String targetMonth = targetDate.substring(0, 7); // YYYY-MM 포맷 변환
        return reserveDAO.countReserveByMonth(targetMonth);
    }

    // pet정보로 예약 가져오기
    public List<ReserveDTO> findReserveByPetNum(Long petNum) {
        return reserveDAO.findReserveByPetNum(petNum);
    }

    public Long limitReserveNum() {
        return reserveDAO.limitReserveNum();
    }
}
