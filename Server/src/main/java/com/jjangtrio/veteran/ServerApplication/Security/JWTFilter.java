package com.jjangtrio.veteran.ServerApplication.Security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jjangtrio.veteran.ServerApplication.dto.PermissionDTO;
import com.jjangtrio.veteran.ServerApplication.service.PermissionService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Configuration
public class JWTFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private PermissionService permissionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 이미지 요청은 JWT 필터를 통과하지 않도록 처리
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/static/")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");

        System.out.println(authorizationHeader);

        // Authorization 헤더를 포함하지 않거나, Bearer로 시작하지 않으면 필터 통과핑
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 순수 토큰 추출핑
        String token = authorizationHeader.replace("Bearer ", "");

        try {
            Claims claims = jwtService.extractToken(token);

            String type = claims.get("type", String.class);
            System.out.println(token);

            if ("manager".equals(type)) {
                Long managerNum = Long.valueOf(claims.get("managerNum").toString());
                PermissionDTO permissionDTO = permissionService.getPermissionByUserNum(managerNum);

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                System.out.println(permissionDTO.getPermissionRole());
                System.out.println(permissionDTO.getPermissionState());
                authorities.add(new SimpleGrantedAuthority(permissionDTO.getPermissionRole()));
                authorities.add(new SimpleGrantedAuthority(permissionDTO.getPermissionState()));
                authorities.add(new SimpleGrantedAuthority("MANAGER")); // Spring Security에서 사용 위해
                System.out.println(managerNum);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        managerNum, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken); // 객체를 SecurityContext에 저장

            } else if ("user".equals(type)) {
                Long userNum = Long.valueOf(claims.get("userNum").toString());

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("USER"));
                authorities.add(new SimpleGrantedAuthority(claims.get("userStatus", String.class)));
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userNum, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

        } catch (Exception e) {
            System.out.println("JWT 검증 실패: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper;
    }
}
