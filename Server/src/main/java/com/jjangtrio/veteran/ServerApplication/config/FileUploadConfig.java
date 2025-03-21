package com.jjangtrio.veteran.ServerApplication.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import jakarta.servlet.MultipartConfigElement;

@Configuration
public class FileUploadConfig {
     @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(10)); // 파일 크기 제한
        factory.setMaxRequestSize(DataSize.ofMegabytes(10)); // 요청 크기 제한
        return factory.createMultipartConfig();
    }
}
