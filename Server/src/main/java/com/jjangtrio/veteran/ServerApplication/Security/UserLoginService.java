package com.jjangtrio.veteran.ServerApplication.Security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.jjangtrio.veteran.ServerApplication.dao.UserDAO;
import com.jjangtrio.veteran.ServerApplication.dto.UserDTO;

@Service
public class UserLoginService implements UserDetailsService {

    @Autowired
    private UserDAO userDAO;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        // UserDTO 객체를 받아 UserForLogin 객체로 변환
        UserDTO userDTO = userDAO.selectUserId(userId);
        if (userDTO == null) {
            throw new UsernameNotFoundException("User not found with username: " + userId);
        }

        // 사용자 활성화 여부 체크 (isEnabled는 userDTO에 없다면 userStatus로 변경)
        if (userDTO.getUserStatus() == null || userDTO.getUserStatus().equals("INACTIVE")) {
            throw new UsernameNotFoundException("User is disabled: " + userId);
        }

        // 권한 설정
        List<GrantedAuthority> authorities = new ArrayList<>();
        // "ROLE_" 접두어 추가
        authorities.add(new SimpleGrantedAuthority("ROLE_" + userDTO.getUserStatus()));

        return new UserForLogin(
                userDTO.getUserNum(),
                userDTO.getUserId(),
                userDTO.getUserPwd(),
                UserStatusRole.fromString(userDTO.getUserStatus()), // Enum 변환
                authorities);
    }
}
