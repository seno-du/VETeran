package com.jjangtrio.veteran.ServerApplication.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jjangtrio.veteran.ServerApplication.dao.PermissionDAO;
import com.jjangtrio.veteran.ServerApplication.dto.PermissionDTO;

@Service
public class PermissionService {

    @Autowired
    private PermissionDAO permissionDAO;

    public PermissionDTO getPermissionByUserNum(Long managerNum) {
        return permissionDAO.getPermissionByUserNum(managerNum);
    }

    @Transactional
    public void insertPermission(PermissionDTO permission) {
        permissionDAO.insertPermission(permission);
    }

    public PermissionDTO getPermissionByPermissonRole(String permissionRole) {
        return permissionDAO.getPermissonRole(permissionRole);
    }
}
