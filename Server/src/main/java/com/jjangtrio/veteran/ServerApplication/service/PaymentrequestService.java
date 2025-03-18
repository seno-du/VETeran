package com.jjangtrio.veteran.ServerApplication.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jjangtrio.veteran.ServerApplication.dao.PaymentrequestDAO;
import com.jjangtrio.veteran.ServerApplication.dto.PaymentrequestDTO;

@Service
public class PaymentrequestService {

    @Autowired
    private PaymentrequestDAO paymentrequestDAO;

    @Transactional
    public void insert(PaymentrequestDTO dto) {
        paymentrequestDAO.insert(dto);
    }

    @Transactional
    public void updateState(String state, String paymentKey, Long userNum, Long reserveNum) {
        if (state.equals(null) || userNum.equals(null))
            return;
        if (state.equals("중단됨") || state.equals("취소됨") || state.equals("완료됨") || state.equals("만료됨")
                || state.equals("진행중") || state.equals("부분 취소됨") || state.equals("준비됨") || state.equals("입금 대기중"))
            paymentrequestDAO.updateState(state, paymentKey, userNum, reserveNum);
    }

    public List<Map<String, Object>> selectPay(int pageSize, int page) {
        int offset = (page - 1) * pageSize;
        return paymentrequestDAO.selectPay(pageSize, offset);
    }

    public int countPay() {
        return paymentrequestDAO.countPay();
    }
}
