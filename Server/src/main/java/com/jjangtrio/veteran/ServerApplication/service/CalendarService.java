package com.jjangtrio.veteran.ServerApplication.service;

import com.jjangtrio.veteran.ServerApplication.dao.CalendarDAO;
import com.jjangtrio.veteran.ServerApplication.dto.CalendarDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalendarService {

    private final CalendarDAO calendarDAO;

    @Autowired
    public CalendarService(CalendarDAO calendarDAO) {
        this.calendarDAO = calendarDAO;
    }

    // 일정 추가
    public void insertCalendar(CalendarDTO calendarDTO) {
        calendarDAO.insertCalendar(calendarDTO);
    }

    // 일정 조회
    public List<CalendarDTO> selectCalender(Long managerNum) {
        return calendarDAO.selectCalender(managerNum);
    }
}
