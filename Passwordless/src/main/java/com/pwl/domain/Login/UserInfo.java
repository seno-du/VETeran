package com.pwl.domain.Login;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {
	private Long managerNum;
	private String managerEmail; // managerId 역할
	private String managerPwd;
	private Date managerSignupDate;
}
