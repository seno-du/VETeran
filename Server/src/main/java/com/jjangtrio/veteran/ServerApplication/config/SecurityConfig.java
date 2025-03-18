package com.jjangtrio.veteran.ServerApplication.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.jjangtrio.veteran.ServerApplication.Security.JWTFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Autowired
    private JWTFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());

        // CORS 설정
        http.cors(cors -> cors.configurationSource(request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(List.of("http://localhost:5000", "http://localhost:6100")); // 구체적인 도메인 명시
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 모든 HTTP 메서드 허용
            config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-PWD-RESET-AUTH","x-api-key")); // 모든 헤더 허용
            config.setAllowCredentials(true); // credentials 허용하면 '*' 사용 불가
            return config;
        }));

        // 모든 요청에 대해 인증 없이 접근 가능]
        http.authorizeHttpRequests(auth -> auth.requestMatchers("/back/api/user/jwt/currentManager").hasAuthority("MANAGER"));
        
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        http.addFilterBefore(jwtFilter, BasicAuthenticationFilter.class);

        // formLogin 비활성화
        http.formLogin(formlogin -> formlogin.disable());

        return http.build();
    }
}
