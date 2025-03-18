package com.jjangtrio.veteran.ServerApplication.controller;

import com.jjangtrio.veteran.ServerApplication.dto.CalendarDTO;
import com.jjangtrio.veteran.ServerApplication.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private CalendarService calendarService;

    @Autowired
    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    // 일정 추가
    @PostMapping("/add")
    public ResponseEntity<?> addCalendar(@RequestBody CalendarDTO calendarDTO) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long managerNum= Long.valueOf(auth.getPrincipal().toString());
            calendarDTO.setManagerNum(managerNum);
            calendarService.insertCalendar(calendarDTO);
            return ResponseEntity.ok("일정 추가가 완료되었습니다.");
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(500).body("일정 추가에 실패했습니다.");
            
        }
    }

    // 일정 조회
    @GetMapping("/list")
    public ResponseEntity<?> getCalendarList() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long managerNum = Long.valueOf(auth.getPrincipal().toString());
            List<CalendarDTO> calendarList = calendarService.selectCalender(managerNum);
            return ResponseEntity.ok(calendarList);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}
