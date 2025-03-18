package com.jjangtrio.veteran.ServerApplication.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.jjangtrio.veteran.ServerApplication.dto.UserDTO;
import com.jjangtrio.veteran.ServerApplication.service.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /** 🔹 모든 사용자 조회 */
    @GetMapping("/all")
    public ResponseEntity<?> selectAllUser() {
        List<UserDTO> users = userService.selectAllUser();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/one")
    public ResponseEntity<?> selectUserNum() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userNum = Long.valueOf(auth.getPrincipal().toString());

            UserDTO user = userService.selectUserNum(userNum);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    // 사용자 이메일 확인 (아이디찾기)
    @PostMapping("/searchId")
    public ResponseEntity<?> selectCountUserEmail(@RequestBody Map<String, Object> userInfo) {

        try {
            UserDTO user = userService.selectUserEmail(userInfo.get("email").toString());

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("일치하는 회원 정보가 없습니다.");
            }

            if (!user.getUserName().equals(userInfo.get("name").toString())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("입력한 정보를 한번 더 확인해주세요.");
            }
            return ResponseEntity.ok(user.getUserId());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @GetMapping("/userPage/{pageNum}")
    public ResponseEntity<?> pageUser(@PathVariable("pageNum") int pageNum) {
        int pageSize = 10; // 프론트에서 변경될 수 있으면 이를 동적으로 처리 가능
        List<UserDTO> users = userService.pageUser(pageNum, pageSize);
        return ResponseEntity.ok(users);
    }

    // 정보 수정
    @PostMapping("/updateUserInfo")
    public ResponseEntity<?> pageUser(@RequestBody Map<String, Object> userInfo) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userNum = Long.valueOf(auth.getPrincipal().toString());

            UserDTO user = userService.selectUserNum(userNum);
            if (user != null && user.getUserStatus().equals("활성")) {
                userInfo.put("userNum", user.getUserNum());

                if(userInfo.get("detailedAddress") != null){
                    userInfo.put("userAddress", userInfo.get("userAddress") + " "  + userInfo.get("detailedAddress"));
                }else{
                    userInfo.put("userAddress", userInfo.get("userAddress"));
                }
                System.out.println("+++++++++++++++++++++" + userInfo);

                userService.updateUserInfo(userInfo);
                return ResponseEntity.status(200).body("회원 정보 수정이 완료되었습니다.");
            }
            return ResponseEntity.status(406).body("존재하지않는 회원입니다.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body("회원 정보 수정에 실패했습니다.");
        }
    }

    // 탈퇴
    @PostMapping("/unsubscription")
    public ResponseEntity<?> unsubscription(@RequestBody Map<String, Object> password) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userNum = Long.valueOf(auth.getPrincipal().toString());

            UserDTO user = userService.selectUserNum(userNum);
            if (user != null && user.getUserStatus().equals("활성")) {
                password.put("userPwd", user.getUserPwd());
                if (userService.verifyPwd(password)) {
                    userService.unsubscription(userNum);
                    return ResponseEntity.ok().body("회원 탈퇴에 성공했습니다.");
                }
            }
            return ResponseEntity.status(406).body("이미 탈퇴한 회원입니다.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body("회원 탈퇴에 실패했습니다.");
        }
    }

    // 비밀번호 확인
    @PostMapping("/verifyPwd")
    public ResponseEntity<?> verifyPwd(@RequestBody Map<String, Object> password) {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userNum = Long.valueOf(auth.getPrincipal().toString());

            UserDTO user = userService.selectUserNum(userNum);
            if (user != null && user.getUserStatus().equals("활성")) {
                password.put("userPwd", user.getUserPwd());
                if (userService.verifyPwd(password))
                    return ResponseEntity.ok().body("비밀번호가 확인되었습니다.");
                return ResponseEntity.status(500).body("비밀번호가 맞지 않습니다.");

            }
            return ResponseEntity.status(406).body("존재하지 않는 회원입니다.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body(e);
        }
    }

    // 비밀번호 변경
    @PostMapping("/changePwd")
    public ResponseEntity<?> changePwd(@RequestBody Map<String, Object> password) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userNum = Long.valueOf(auth.getPrincipal().toString());

            UserDTO user = userService.selectUserNum(userNum);
            if (user != null && user.getUserStatus().equals("활성")) {
                password.put("userNum", user.getUserNum());
                password.put("userPwd", user.getUserPwd());
                if (userService.verifyPwd(password)) {
                    if (userService.changePwd(password))
                        return ResponseEntity.ok().body("비밀번호가 변경되었습니다.");
                    return ResponseEntity.status(500).body("비밀번호 형식을 확인해주세요.");
                }
            }
            return ResponseEntity.status(406).body("존재하지 않는 회원입니다.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body(e);
        }
    }
}
