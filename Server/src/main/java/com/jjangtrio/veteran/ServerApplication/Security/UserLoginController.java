package com.jjangtrio.veteran.ServerApplication.Security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.jjangtrio.veteran.ServerApplication.dto.UserDTO;
import com.jjangtrio.veteran.ServerApplication.service.CoolSMSService;
import com.jjangtrio.veteran.ServerApplication.service.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@RestController
@RequestMapping("/api/user/jwt")
public class UserLoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CoolSMSService coolSMSService;

    @PostMapping("/login")
    public ResponseEntity<?> userLogin(@RequestBody Map<String, String> map) {
        try {
            String userId = map.get("userId");
            String userPwd = map.get("userPwd");

            UserDTO user = userService.selectUserId(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("아이디 또는 비밀번호를 확인해주세요.");
            }

            if (!passwordEncoder.matches(userPwd.toString(), user.getUserPwd())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("아이디 또는 비밀번호를 확인해주세요.");
            }

            if (user.getUserStatus().equals("비활성") || user.getUserStatus().equals("INACTIVE")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("탈퇴한 사용자입니다.");
            }

            // 인증 객체 생성
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userId, userPwd);

            // 인증 정보 확인
            org.springframework.security.core.Authentication auth = authenticationManager
                    .authenticate(authenticationToken);

            // 인증 정보를 SecurityContext에 설정
            SecurityContextHolder.getContext().setAuthentication(auth);

            // JWT 토큰 생성
            String token = jwtService.createToken(auth);

            // JSON 형태로 응답 반환
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (UsernameNotFoundException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("로그인 실패");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("자격 증명에 실패하였습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("로그인 실패: ");
        }
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> userTokenforResetPassword(@RequestBody Map<String, String> map) {
        try {
            String userId = map.get("userId");
            String userPhone = map.get("userPhone");

            UserDTO user = userService.selectUserPhone(userPhone);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("등록되지않은 번호입니다.");
            }

            if (!user.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("아이디를 확인해주세요.");
            }

            if (user.getUserStatus().equals("비활성") || user.getUserStatus().equals("INACTIVE")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("탈퇴한 회원입니다.");
            }

            String token = jwtService.createTemporaryPasswordResetToken(user);

            Map<String, String> response = new HashMap<>();
            response.put("token", token);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("비밀번호 재설정용 토큰 발급 오류 발생 : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("비밀번호 재설정용 토큰 발급 오류 발생");
        }
    }

    // 비밀번호 재설정
    @PostMapping("/newPassword")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, Object> password,
            @RequestHeader("X-PWD-RESET-AUTH") String authorizationHeader) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");

            Claims claims = jwtService.extractToken(token);

            Integer userNum = claims.get("userNum", Integer.class);

            password.put("userNum", userNum.longValue());

            if (userService.changePwd(password))
                return ResponseEntity.ok().body("비밀번호가 변경되었습니다.");

            return ResponseEntity.status(500).body("비밀번호 형식을 확인해주세요.");

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body("비밀번호 변경에 실패했습니다.");
        }
    }

    @PostMapping("/login/social")
    public ResponseEntity<?> userLoginSocial(@RequestBody Map<String, String> map) {
        try {
            String userEmail = map.get("userEmail");
            if (userEmail == null || userEmail.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이메일이 필요합니다.");
            }

            // DB에서 이메일로 사용자 조회
            UserDTO user = userService.selectCountUserEmail(userEmail);
            if (user == null) {
                return ResponseEntity.ok("is not exist");
            } else if (user.getUserStatus() == "비활성" || user.getUserStatus() == "INACTIVE") {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("비활성 상태의 사용자입니다.");
            }

            // DB에서 가져온 사용자 정보 (예: userId 등)
            UserForLogin userForLogin = new UserForLogin(
                    user.getUserNum(),
                    user.getUserId(),
                    user.getUserPwd(), // 실제 비밀번호는 사용하지 않음
                    UserStatusRole.fromString(user.getUserStatus()),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

            // 인증된 토큰을 직접 생성 (비밀번호 검증을 우회)
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userForLogin, null, userForLogin.getAuthorities());

            // SecurityContext에 직접 설정
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            // JWT 토큰 생성
            String token = jwtService.createToken(authenticationToken);

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("로그인 실패: " + e.getMessage());
        }
    }

    // 아이디 중복 검사
    @GetMapping("/idcheck")
    public ResponseEntity<?> idcheck(@RequestParam("userId") String userId) {
        try {
            boolean success = userService.existsByUserId(userId);
            return ResponseEntity.ok(success);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserDTO userDTO
    , @RequestHeader("Authorization") String token
    ) {
        try {
            String uuid = jwtService.extractToken(token).get("uuid").toString();
            if (!coolSMSService.verifyPasscode(uuid)) {
            return ResponseEntity.status(400).body("PHONE_NOTAUTH");
            }

            // 중복검사(휴대 전화)
            if (!userService.existsByUserPhone(userDTO.getUserPhone())) {
                return ResponseEntity.status(409).body("PHONE_DUPLICATE");
            }
            // 중복검사(이메일)
            if (!userService.existsByUserEmail(userDTO.getUserEmail())) {
                return ResponseEntity.status(409).body("EMAIL_DUPLICATE");
            }
            // 중복검사(아이디)
            if (!userService.existsByUserId(userDTO.getUserId())) {
                return ResponseEntity.status(409).body("ID_DUPLICATE");
            }
            // 아이디 유효성 검사
            if (userDTO.getUserId() == null || userDTO.getUserId().contains(" ") ||
                    !userDTO.getUserId().matches("^[a-zA-Z0-9_+&*-]+$")) {
                return ResponseEntity.status(409).body("NONE_ID_TYPE");
            }
            // 이메일 유효성 검사
            if (userDTO.getUserEmail() == null || userDTO.getUserEmail().contains(" ") ||
                    !userDTO.getUserEmail()
                            .matches("[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}")) {
                return ResponseEntity.status(409).body("NONE_EMAIL_TYPE");
            }
            // 비밀번호 유효성 검사 (8자 이상, 영문+숫자+특수문자 포함)
            String passwordRegex = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$";
            if (!Pattern.matches(passwordRegex, userDTO.getUserPwd())) {
                return ResponseEntity.status(409).body("NONE_PASSWORD_TYPE");
            }
            // 이름 공백 검사
            if (userDTO.getUserName().contains(" ")) {
                return ResponseEntity.status(409).body("TRIM_IN_NAME");
            }
            // 비밀번호 공백 검사
            if (userDTO.getUserPwd().contains(" ")) {
                return ResponseEntity.status(409).body("TRIM_IN_PWD");
            }

            String encodedPassword = passwordEncoder.encode(userDTO.getUserPwd());
            userDTO.setUserPwd(encodedPassword);
            boolean success = userService.insertUser(userDTO);

            if (success) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "회원가입이 성공적으로 완료되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                throw new RuntimeException("회원가입에 실패했습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/verifycode")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> requestBody) {
        try {
            String token = requestBody.get("token");
            String inputCode = requestBody.get("code");
            System.out.println(token);

            String uuid = jwtService.extractToken(token).get("uuid").toString();
            Map<String, Object> response = new HashMap<>();

            if (!coolSMSService.verifyPasscode(uuid, inputCode)) {
                response.put("result", false);
                return ResponseEntity.status(400).body(response);
            }

            response.put("result", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("여기 오류남");
            System.out.println(e);
            return ResponseEntity.status(400).build();
        }

    }

    @GetMapping("/currentManager")
    public ResponseEntity<?> currentManager() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            List<String> result = new ArrayList<>();
            for (GrantedAuthority auth : authentication.getAuthorities()) {
                result.add(auth.getAuthority());
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

}
