package com.jjangtrio.veteran.ServerApplication.dao;

import org.apache.ibatis.annotations.Mapper;
import com.jjangtrio.veteran.ServerApplication.dto.PermissionDTO;

@Mapper
public interface PermissionDAO {

    PermissionDTO getPermissionByUserNum(Long managerNum);

    void insertPermission(PermissionDTO permission);

    PermissionDTO getPermissonRole(String permissionRole);
}
