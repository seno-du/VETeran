package com.jjangtrio.veteran.ServerApplication.API.Nager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/naver")
@CrossOrigin("http://localhost:5000")
public class NaverLoginController {

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientPwd;

    @Value("${naver.callback.url}")
    private String callbackURL;

    @PostMapping("/login")
    public ResponseEntity<?> login() throws UnsupportedEncodingException {
        String rediretURI = URLEncoder.encode(this.callbackURL, "UTF-8");
        SecureRandom random = new SecureRandom();
        String state = new BigInteger(130, random).toString();
        String apiURL = "https://nid.naver.com/oauth2.0/authorize?response_type=code";
        apiURL += "&client_id=" + this.clientId;
        apiURL += "&redirect_uri=" + rediretURI;
        apiURL += "&state=" + state;

        return ResponseEntity.ok().body(apiURL);
    }

    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam(value = "code") String code,
            @RequestParam(value = "state") String state) throws UnsupportedEncodingException {
        String redirectURI = URLEncoder.encode(this.callbackURL, "UTF-8");
        String apiURL = "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code&"
                + "client_id=" + this.clientId + "&client_secret=" + this.clientPwd
                + "&redirect_uri=" + redirectURI + "&code=" + code + "&state=" + state;

        String accessToken = "";
        String refreshToken = "";

        try {
            // 액세스 토큰 요청
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            BufferedReader br;

            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            String inputLine;
            StringBuffer res = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                res.append(inputLine);
            }
            br.close();

            // 액세스 토큰 추출
            String response = res.toString();
            // 여기서 액세스 토큰을 파싱하여 access_token을 추출해야 함
            // 예시: {"access_token":"ACCESS_TOKEN", "refresh_token":"REFRESH_TOKEN", ...}
            accessToken = extractAccessToken(response); // `extractAccessToken` 메서드를 구현해야 함

            // 네이버 사용자 정보 요청
            String userInfoURL = "https://openapi.naver.com/v1/nid/me";
            URL userInfoRequestURL = new URL(userInfoURL);
            HttpURLConnection userInfoCon = (HttpURLConnection) userInfoRequestURL.openConnection();
            userInfoCon.setRequestMethod("GET");
            userInfoCon.setRequestProperty("Authorization", "Bearer " + accessToken);

            int userInfoResponseCode = userInfoCon.getResponseCode();
            BufferedReader userInfoBr;

            if (userInfoResponseCode == 200) {
                userInfoBr = new BufferedReader(new InputStreamReader(userInfoCon.getInputStream()));
            } else {
                userInfoBr = new BufferedReader(new InputStreamReader(userInfoCon.getErrorStream()));
            }

            StringBuffer userInfoRes = new StringBuffer();
            while ((inputLine = userInfoBr.readLine()) != null) {
                userInfoRes.append(inputLine);
            }
            userInfoBr.close();

            // 사용자 정보에서 이메일 추출
            String userInfo = userInfoRes.toString();
            String email = extractEmail(userInfo); // `extractEmail` 메서드를 구현해야 함

            Map<String, String> map = new HashMap<>();
            map.put("email", email);
            map.put("accessToken", accessToken);
            map.put("token", response);
            // 이메일을 프론트엔드로 전달
            return ResponseEntity.ok().body(map);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Error occurred during token exchange or user info retrieval: " + e.getMessage());
        }
    }

    // 액세스 토큰을 응답에서 추출하는 메서드 (예시)
    private String extractAccessToken(String response) {
        // JSON 응답에서 액세스 토큰을 추출하는 로직 구현
        // 예시에서는 단순히 액세스 토큰만 추출하는 방식입니다.
        // 실제로는 JSON 파싱 라이브러리(e.g., Jackson, Gson 등)를 사용하여 구현해야 합니다.
        String token = response.split("\"access_token\":\"")[1].split("\"")[0];
        return token;
    }

    // 이메일을 응답에서 추출하는 메서드 (예시)
    private String extractEmail(String response) {
        // JSON 응답에서 이메일을 추출하는 로직 구현
        // 예시에서는 단순히 이메일만 추출하는 방식입니다.
        // 실제로는 JSON 파싱 라이브러리(e.g., Jackson, Gson 등)를 사용하여 구현해야 합니다.
        String email = response.split("\"email\":\"")[1].split("\"")[0];
        return email;
    }

}
