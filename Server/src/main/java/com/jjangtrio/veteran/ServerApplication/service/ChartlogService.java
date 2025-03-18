package com.jjangtrio.veteran.ServerApplication.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jjangtrio.veteran.ServerApplication.dao.ChartlogDAO;
import com.jjangtrio.veteran.ServerApplication.dto.ChartlogDTO;

@Service
public class ChartlogService {

    @Autowired
    private ChartlogDAO chartlogDAO;

    @Transactional
    public void insertChartLog(ChartlogDTO chartlog) {
        chartlogDAO.insertChartLog(chartlog);
    }

}
