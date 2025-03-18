package com.jjangtrio.veteran.ServerApplication.dao;

import org.apache.ibatis.annotations.Mapper;
import com.jjangtrio.veteran.ServerApplication.dto.ChartlogDTO;

@Mapper
public interface ChartlogDAO {

    void insertChartLog(ChartlogDTO chartlogDTO);
}
