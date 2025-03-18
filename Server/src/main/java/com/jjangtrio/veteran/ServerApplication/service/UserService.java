package com.jjangtrio.veteran.ServerApplication.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jjangtrio.veteran.ServerApplication.dao.UserDAO;
import com.jjangtrio.veteran.ServerApplication.dto.UserDTO;

@Service
public class UserService {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 사용자 추가 (회원가입)
    public boolean insertUser(UserDTO userDTO) {
        try {
            userDAO.insertUser(userDTO);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 모든 사용자 조회
    public List<UserDTO> selectAllUser() {
        return userDAO.selectAllUser();
    }

    // 특정 사용자 조회
    public UserDTO selectUserNum(Long userNum) {
        return userDAO.selectUserNum(userNum);
    }

    public UserDTO selectUserName(String userName) {
        return userDAO.selectUserName(userName);
    }

    public UserDTO selectUserEmail(String userEmail) {
        return userDAO.selectUserEmail(userEmail);
    }

    public UserDTO selectUserPhone(String userPhone) {
        return userDAO.selectUserPhone(userPhone);
    }

    // 회원가입시 아이디 조회
    public UserDTO selectUserId(String userId) {
        return userDAO.selectUserId(userId);
    }

    // 사용자 로그인 시 아이디로 DB PW와 받아온 PW 비교
    public UserDTO selectUserStatus(String userId, String userPwd) {

        UserDTO user = userDAO.selectUserId(userId);
        if (userPwd == userDAO.selectUserId(userId).getUserPwd()) {
            return user;
        } else {
            return userDAO.selectUserId(userId);
        }
    }

    // 이메일 전달
    public UserDTO selectCountUserEmail(String userEmail) {
        return userDAO.selectUserEmail(userEmail);
    }

    // 회원가입시 전화번호 조회
    @Transactional
    public boolean existsByUserPhone(String userPhone) {
        if (0 == userDAO.existsByUserPhone(userPhone)) {
            return true;
        } else {
            return false;
        }
    }

    // 회원가입시 이메일 조회
    @Transactional
    public boolean existsByUserEmail(String userEmail) {
        if (0 == userDAO.existsByUserEmail(userEmail)) {
            return true;
        } else {
            return false;
        }
    }

    // 회원가입시 아이디 조회
    @Transactional
    public boolean existsByUserId(String userId) {
        System.out.println(userId);
        if (0L == userDAO.existsByUserId(userId)) {
            return true;
        } else {
            return false;
        }
    }

    public List<UserDTO> pageUser(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return userDAO.pageUser(offset, pageSize);
    }

    // 회원 정보 수정
    public void updateUserInfo(Map<String, Object> userInfo) {
        userDAO.updateUserInfo(userInfo);
    }
 
    // 비밀번호 확인
    public Boolean verifyPwd(Map<String, Object> password){
        return passwordEncoder.matches(password.get("currentPassword").toString(),password.get("userPwd").toString()); 
        //matches(CharSequence rawPassword, String encodedPassword);
    }

    // 비밀번호 수정
    public Boolean changePwd(Map<String, Object> password){
        String passwordRegex = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
            if (!Pattern.matches(passwordRegex, password.get("newPassword").toString())) return false;
            if (password.get("newPassword").toString().contains(" ")) return false;

        String newPassword = passwordEncoder.encode(password.get("newPassword").toString());
        password.put("newPassword", newPassword);
        userDAO.changePwd(password);
        return true;
    }

    // 탈퇴하기
    public void unsubscription(Long userNum) {
        userDAO.unsubscription(userNum);
    }

}
