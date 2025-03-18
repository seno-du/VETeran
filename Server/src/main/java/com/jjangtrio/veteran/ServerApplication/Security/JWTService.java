package com.jjangtrio.veteran.ServerApplication.Security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.jjangtrio.veteran.ServerApplication.dto.UserDTO;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTService {

    private final SecretKey key;

    @Value("${jwt.userTokenExpiration}")
    private Long userTokenExpiration; // 만료 시간 (밀리초 단위로 설정해야 함)

    public JWTService(@Value("${jwt.secret}") String secret) {
        System.out.println("JWT secret: " + secret);
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String createToken(Authentication authentication) {
        UserForLogin userForLogin = (UserForLogin) authentication.getPrincipal();
        Long userNum = userForLogin.getUserNum();
        String userStatus = userForLogin.getUserStatus().name();
        // JWT 토큰 생성
        String token = Jwts.builder()
                .claim("userNum", userNum)
                .claim("userStatus", userStatus)
                .claim("type", "user")
                .setIssuedAt(new Date()) // 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + userTokenExpiration)) // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        return token;
    }

    // 토큰에서 Claims 추출
    public Claims extractToken(String token) {
        try {

            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (JwtException e) {
            System.out.println("error" + e.getMessage());
            throw new JwtException("Invalid JWT token", e); // JWT 관련 예외 처리
        } catch (Exception e) {
            throw new RuntimeException("Token parsing error", e); // 그 외 예외 처리
        }
    }

    // 비밀번호 재설정용 임시토큰 발행
    public String createTemporaryPasswordResetToken(UserDTO userDTO) {

        Date expirationTime = new Date(System.currentTimeMillis() + 1800000L); // 30분

        return Jwts.builder()
                .setSubject(userDTO.getUserPhone())
                .claim("userNum", userDTO.getUserNum())
                .setExpiration(expirationTime)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

}
