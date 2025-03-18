package com.jjangtrio.veteran.ServerApplication.API.Google;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/login")
@CrossOrigin(origins = { "http://localhost:5000" })
public class GoogleController {

        @Value("${google.client.id}")
        private String googleClientId;

        @Value("${google.client.pw}")
        private String googleClientPw;

        // 로그인 URL을 생성하여 반환 (로그인 및 캘린더 연동을 포트에 맞게 처리)
        @PostMapping("/google")
        private ResponseEntity<?> loginUrlGoogle(HttpServletRequest request) {
                String origin = request.getHeader("Origin"); // 요청을 보낸 프론트엔드의 주소를 가져옴
                String callbackUrl = getCallbackUrl(origin);

                // 구글 로그인 URL 생성
                String reqUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id="
                                + googleClientId
                                + "&redirect_uri=" + callbackUrl
                                + "&response_type=code"
                                + "&scope=email%20profile%20openid"
                                + "&access_type=offline";

                // 요청한 포트에 맞춰 로그인 URL을 반환
                return ResponseEntity.ok().body(reqUrl);
        }

        // 구글 로그인 후 콜백받은 인증 코드로 토큰을 받아와서 처리
        @GetMapping("/google")
        private ResponseEntity<?> loginGoogle(@RequestParam(value = "code") String authCode,
                        HttpServletRequest request) {

                String origin = request.getHeader("Origin");
                String callbackUrl = getCallbackUrl(origin);

                RestTemplate restTemplate = new RestTemplate();

                // GoogleRequest 객체 생성
                GoogleRequest googleRequest = GoogleRequest.builder()
                                .clientId(googleClientId)
                                .clientSecret(googleClientPw)
                                .code(authCode)
                                .redirectUri(callbackUrl)
                                .grantType("authorization_code")
                                .build();

                System.out.println(googleRequest);

                // Google OAuth 서버에서 Access Token 받기
                ResponseEntity<GoogleResponse> googleResponseEntity = restTemplate.postForEntity(
                                "https://oauth2.googleapis.com/token",
                                googleRequest,
                                GoogleResponse.class);

                // access_token 및 id_token을 가져옴
                String jwtToken = googleResponseEntity.getBody().getId_token();

                // Google OAuth 서버에서 사용자 정보 가져오기
                ResponseEntity<Map> userInfoResponse = restTemplate.getForEntity(
                                "https://oauth2.googleapis.com/tokeninfo?id_token=" + jwtToken,
                                Map.class);

                @SuppressWarnings("unchecked")
                Map<String, Object> userInfo = userInfoResponse.getBody();
                String email = (String) userInfo.get("email");

                return ResponseEntity.ok().body(Collections.singletonMap("email", email));

        }

        private String getCallbackUrl(String origin) {
                Map<String, String> callbackUrls = Map.of(
                                "http://localhost:5000", "http://localhost:5000/login/google/callback");

                String callbackUrl = callbackUrls.get(origin);
                if (callbackUrl == null) {
                        System.out.println("Invalid origin: " + origin); // 디버깅 로그
                }
                return callbackUrl;
        }

}
