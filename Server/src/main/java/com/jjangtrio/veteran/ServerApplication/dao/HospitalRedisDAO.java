package com.jjangtrio.veteran.ServerApplication.dao;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class HospitalRedisDAO {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    //개별 입원환자
    public String getHospitalRedisNum(){
        return stringRedisTemplate.opsForValue().get("data");
    };

    // 당일 입원환자 목록 조회
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getHospitalRedis(){
        return (List<Map<String, Object>>) redisTemplate.opsForValue().get("todayHospital");
    };

    // 당일 입원환자 목록 저장
    public void setHospitalRedis(List<Map<String, Object>> hospitalData) {
        redisTemplate.opsForValue().set("todayHospital", hospitalData, Duration.ofMinutes(1));
    }
}
