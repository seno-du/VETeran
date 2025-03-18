package com.jjangtrio.veteran.ServerApplication.dao;

import org.apache.ibatis.annotations.Mapper;

import com.jjangtrio.veteran.ServerApplication.dto.SupplierDTO;

@Mapper
public interface SupplierDAO {

    void insertSupplier(SupplierDTO supplierdto);

    String selectSupplierTransactionId(String supplierCode);

}
