package com.jjangtrio.veteran.ServerApplication.Security;

public enum UserStatusRole {
    ACTIVE("활성"),
    INACTIVE("비활성");

    private final String status;

    // 생성자
    UserStatusRole(String status) {
        this.status = status;
    }

    // status 값을 반환하는 메서드
    public String getStatus() {
        return status;
    }

    // 문자열로부터 enum을 반환하는 메서드
    public static UserStatusRole fromString(String status) {
        for (UserStatusRole role : UserStatusRole.values()) {
            if (role.getStatus().equals(status)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + status); // 예외 처리
    }
}
