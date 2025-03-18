package com.jjangtrio.veteran.ServerApplication.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jjangtrio.veteran.ServerApplication.dao.SupplierDAO;
import com.jjangtrio.veteran.ServerApplication.dto.SupplierDTO;

@Service
public class SupplierService {

    @Autowired
    private SupplierDAO supplierDAO;

    public void insertSupplier(SupplierDTO supplierdto) {
        supplierDAO.insertSupplier(supplierdto);
    }

    public String selectSupplierTransactionId(String selectSupplierTransactionId) {
        return supplierDAO.selectSupplierTransactionId(selectSupplierTransactionId);
    }

}
