package com.jjangtrio.veteran.ServerApplication.dao;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository

public class CoolSMSDAO {
    private final StringRedisTemplate redisTemplate;

    public String getUserPhone(String UserPhone){
        return redisTemplate.opsForValue().get(UserPhone);
    }
}
