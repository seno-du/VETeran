package com.jjangtrio.veteran.ServerApplication.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jjangtrio.veteran.ServerApplication.dto.ChartlogDTO;
import com.jjangtrio.veteran.ServerApplication.service.ChartlogService;

@RestController
@RequestMapping("/api/chartlog")
public class ChartlogController {

    @Autowired
    private ChartlogService chartlogService;

    @PostMapping("/insert")
    public ResponseEntity<?> insertChartlog(@RequestBody List<ChartlogDTO> chartlogs) {
        try {
            for (ChartlogDTO chartlog : chartlogs) {
                chartlogService.insertChartLog(chartlog);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("오류 발생: " + e.getMessage());
        }
    }

}
