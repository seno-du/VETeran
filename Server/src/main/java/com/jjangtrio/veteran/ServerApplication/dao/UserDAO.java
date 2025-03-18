package com.jjangtrio.veteran.ServerApplication.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import com.jjangtrio.veteran.ServerApplication.dto.UserDTO;

import io.lettuce.core.dynamic.annotation.Param;

@Mapper
public interface UserDAO {

    void insertUser(UserDTO userDTO);

    List<UserDTO> selectAllUser();

    UserDTO selectUserNum(Long userNum);

    UserDTO selectUserName(String userName);

    UserDTO selectUserId(String userId);

    UserDTO selectUserEmail(String userEmail);

    UserDTO selectUserPhone(String userPhone);

    // 회원가입 중복검사
    int existsByUserEmail(String userEmail);

    int existsByUserPhone(String userPhone);

    Long existsByUserId(String userId);

    List<UserDTO> pageUser(@Param("offset") int offset, @Param("pageSize") int pageSize);

    // 회원 정보 수정
    void updateUserInfo(Map<String, Object> userInfo);

    // 비밀번호 변경
    void changePwd(Map<String, Object> changePwd);

    // 탈퇴
    void unsubscription(Long userNum);

}
