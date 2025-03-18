package com.jjangtrio.veteran.ServerApplication.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jjangtrio.veteran.ServerApplication.dao.PaymentcheckDAO;

@Service
public class PaymentcheckService {

    @Autowired
    private PaymentcheckDAO paymentcheckDAO;

    public List<Map<String, Object>> selectList(int pageSize, int page) {
        int offset = (page - 1) * pageSize;
        return paymentcheckDAO.selectList(pageSize, offset);
    }

    public int selectCount() {
        return paymentcheckDAO.selectCount();
    }
}
