package com.jjangtrio.veteran.ServerApplication.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.jjangtrio.veteran.ServerApplication.dto.CalendarDTO;

@Mapper
public interface CalendarDAO {

    void insertCalendar(CalendarDTO calendarDTO);

    List<CalendarDTO> selectCalender(Long managerNum);

}
