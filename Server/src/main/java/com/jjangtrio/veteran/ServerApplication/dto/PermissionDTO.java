package com.jjangtrio.veteran.ServerApplication.dto;

import org.apache.ibatis.type.Alias;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Alias("permissiondto")
public class PermissionDTO {
    private Long managerNum;
    private String permissionRole;
    private String permissionState;
    private String permissionPasswordless;
}
