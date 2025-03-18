package com.jjangtrio.veteran.ServerApplication.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jjangtrio.veteran.ServerApplication.dao.ManagerDAO;
import com.jjangtrio.veteran.ServerApplication.dto.ManagerDTO;
import com.jjangtrio.veteran.ServerApplication.dto.PageDTO;

@Service
public class ManagerService {

    @Autowired
    private ManagerDAO managerDAO;

    public List<ManagerDTO> getAllManagers() {
        return managerDAO.findAllManager();
    }

    /** 모든 Manager 조회 */
    public List<Map<String, Object>> getAllManagersWithPermissions(PageDTO pageDTO) {
        return managerDAO.findAllWithPermissions(pageDTO.getStartIndex(), pageDTO.getPageSize());
    }

    public long getTotalCount() {
        return managerDAO.totalCount();
    }

    /** 특정 Manager 조회 */
    public Map<String, Object> getManagerById(Long managerNum) {
        return managerDAO.findById(managerNum);
    }

    // 매니저 추가
    @Transactional
    public void insertManager(ManagerDTO manager) {
        managerDAO.insertManager(manager);
    }

    // Manager 정보 업데이트
    public int updateManager(Long managerNum, String permissionState, String managerAddress, String managerPhone) {
        return managerDAO.updateManager(managerNum, permissionState, managerAddress, managerPhone);
    }

    // 의사 이름 불러오기
    public List<Map<String, Object>> findManagersBySubstring() {
        return managerDAO.findManagersBySubstring();
    }

    // 메니저 ID로 이름 찾기
    public List<Map<String, Object>> selectManagerIdWhereRHK(String roc) {
        return managerDAO.selectManagerIdWhereRHK(roc);
    }

    @Transactional
    public List<Map<String, Object>> selectPermissonRole(String permissionRole, PageDTO pageDTO) {
        return managerDAO.selectPermissonRole(permissionRole, pageDTO.getStartIndex(), pageDTO.getPageSize());
    }

    // 매매니저 ID 검사
    public String selectManagerIdCount(String role) {
        // 테크니션일 경우 (role == "0")
        if (role.equals("0")) {
            Long count = managerDAO.selectManagerIdCount(1, "0");
            count += 1;
            String counts = String.format("%02d", count); // 예: 01, 02 등
            return counts; // 이 값이 managerId로 사용될 수 있도록 반환
        }

        // 수의사 역할 (role == 4, 5, 6, 7, 8)
        if (role.length() == 1
                && (role.equals("4") || role.equals("5") || role.equals("6") || role.equals("7") || role.equals("8"))) {
            Long count = managerDAO.selectManagerIdCount(1, role);
            count += 1;
            String counts = String.format("%02d", count);
            counts = role + counts; // 예: 4XX, 5XX 등
            return counts;
        }

        // 매니저 역할 (role == 98, 99)
        if (role.length() == 2 && (role.equals("98") || role.equals("99"))) {
            Long count = managerDAO.selectManagerIdCount(2, role);
            if (role.equals("99")) {
                count += 5;
            } else if (role.equals("98")) {
                count += 1;
            }
            String counts = String.format("%01d", count); // 예: 1, 2, 3 등
            String result = role + counts;
            return result;
        }

        return null; // 올바르지 않은 역할 처리
    }

    public Long selectXCount() {
        return managerDAO.selectXCount();
    }

}
