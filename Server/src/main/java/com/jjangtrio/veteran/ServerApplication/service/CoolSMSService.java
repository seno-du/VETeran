package com.jjangtrio.veteran.ServerApplication.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Objects;

@Service
public class CoolSMSService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public boolean verifyPasscode(String uuid, String inputCode) {
        String redisCode = this.redisTemplate.opsForValue().get(uuid).toString();

        if(!inputCode.equals(redisCode)) {
            return false;
        } else {
            this.redisTemplate.opsForValue().set(uuid, "TRUE", Duration.ofMinutes(5));
            return true;
        }

    }
    @Transactional
    public boolean verifyPasscode(String uuid) {
        String redisCode = this.redisTemplate.opsForValue().get(uuid).toString();

        return Objects.equals(redisCode, "TRUE");
    }

}
 