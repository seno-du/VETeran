package com.jjangtrio.veteran.ServerApplication.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jjangtrio.veteran.ServerApplication.dao.VaccineDAO;
import com.jjangtrio.veteran.ServerApplication.dto.PageDTO;

@Service
public class VaccineService {
    
    @Autowired
    private VaccineDAO vaccineDAO;

    public List<Map<String, Object>> findAllVaccine(PageDTO pageDTO) {
        return vaccineDAO.findAllVaccine(pageDTO.getStartIndex(), pageDTO.getPageSize());
    }

    public long totalCount() {
        return vaccineDAO.totalCount();
    }

    public Map<String, Object> selectVaccine(Long vaccineNum) {
        return vaccineDAO.selectVaccine(vaccineNum);
    }

    public void updateVaccine(Long vaccineNum, Long managerNum, Long petNum, String itemId) {
        vaccineDAO.updateVaccine(vaccineNum, managerNum, petNum, itemId);
    }

    public void insertVaccine(Long managerNum, Long petNum, String itemId) {
        vaccineDAO.insertVaccine(managerNum, petNum, itemId);
    }

}
