package com.jjangtrio.veteran.ServerApplication.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jjangtrio.veteran.ServerApplication.dao.ItemDAO;
import com.jjangtrio.veteran.ServerApplication.dto.ItemDTO;

@Service
public class ItemService {

    @Autowired
    private ItemDAO itemDAO;

    public void insertItem(ItemDTO itemDTO) {
        itemDAO.insertItem(itemDTO);
    }

    public Long count() {
        return itemDAO.count();
    }

    


}
