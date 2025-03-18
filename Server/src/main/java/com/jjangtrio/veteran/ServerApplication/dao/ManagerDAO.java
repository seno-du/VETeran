package com.jjangtrio.veteran.ServerApplication.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jjangtrio.veteran.ServerApplication.dto.ManagerDTO;

@Mapper
public interface ManagerDAO {

        List<ManagerDTO> findAllManager();

        // 모든 Manager 조회
        List<Map<String, Object>> findAllWithPermissions(@Param("start") long start, @Param("size") long size);

        long totalCount();

        Map<String, Object> findById(Long managerNum); // 특정 Manager 조회

        void insertManager(ManagerDTO manager); // Manager 추가

        String findBymanagerNum(@Param("managerNum") Long managerNum);

        int updateManager(@Param("managerNum") Long managerNum,
                        @Param("permissionState") String permissionState,
                        @Param("managerAddress") String managerAddress,
                        @Param("managerPhone") String managerPhone);

        List<Map<String, Object>> findManagersBySubstring();

        List<Map<String, Object>> selectManagerIdWhereRHK(String roc);

        List<Map<String, Object>> selectPermissonRole(
                        @Param("permissionRole") String permissionRole,
                        @Param("start") long start,
                        @Param("size") long size);

        Long selectManagerIdCount(@Param("count") int count, @Param("number") String number);

        Long selectXCount();
}
