package com.jjangtrio.veteran.ServerApplication.dao;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChataiDAO {

    public void saveChatHistory(Map<String, Object> resultMap);

    public List<Map<String, Object>> getChatHistoryByDate(Map<String, Object> map);

    public List<Timestamp> getChatDateInfo(Long userNum);
}