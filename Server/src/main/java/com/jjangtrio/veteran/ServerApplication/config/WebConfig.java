package com.jjangtrio.veteran.ServerApplication.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // React 앱이 실행되는 도메인만 허용
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5000", "http://localhost:6100", "http://localhost:8000", "http://182.220.224.44")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true)
                .allowedHeaders("Authorization", "Content-Type", "x-api-key");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = "file:" + new File("Veteran_Back/Server/src/main/resources/static/uploads").getAbsolutePath() + "/";


        // ✅ 리소스 핸들러 추가 (정적 파일 제공)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadDir)
                // .addResourceLocations("file:D:/ICTStudy/final_project/VETeran_Back/Server/src/main/resources/static/uploads/")  
                .setCachePeriod(3600); // 캐시 유지시간 설정

    }
    

    /**
     * WebMvcConfigurer.super.addViewControllers()를 오버라이딩하여,
     * "/" 경로에 대한 View Controller를 추가합니다.
     * View의 이름을 "index"로 설정하여, src/main/resources/templates/index.html을
     * 반환하게 됩니다.
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
    }

}
