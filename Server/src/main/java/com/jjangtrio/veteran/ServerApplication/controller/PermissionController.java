package com.jjangtrio.veteran.ServerApplication.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jjangtrio.veteran.ServerApplication.dto.PermissionDTO;
import com.jjangtrio.veteran.ServerApplication.service.PermissionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/permission")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @GetMapping("/getPermissionByUserNum")
    public ResponseEntity<?> getPermissionByUserNum(@RequestParam Long managerNum) {

        return ResponseEntity.ok(permissionService.getPermissionByUserNum(managerNum));
    }

    @PostMapping("/insert")
    public ResponseEntity<?> insertPermission(@RequestBody PermissionDTO permission) {
        permissionService.insertPermission(permission);
        return ResponseEntity.ok().build();
    }

}
