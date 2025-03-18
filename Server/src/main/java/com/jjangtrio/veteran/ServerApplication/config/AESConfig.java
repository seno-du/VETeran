package com.jjangtrio.veteran.ServerApplication.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AESConfig {

    @Value("${aes_iv}")
    private String iv;

    @Value("${aes_secret_key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        if (iv == null || secretKey == null) {
            throw new IllegalStateException("Error: IV 또는 SecretKey 값이 설정되지 않았습니다.");
        }
    }

    public String getIv() {
        return iv;
    }

    public String getSecretKey() {
        return secretKey;
    }
}
