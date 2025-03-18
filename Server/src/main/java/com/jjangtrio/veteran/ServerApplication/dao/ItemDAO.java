package com.jjangtrio.veteran.ServerApplication.dao;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jjangtrio.veteran.ServerApplication.dto.ItemDTO;

@Mapper
public interface ItemDAO {

    void insertItem(ItemDTO itemDTO);

    Long count();

}
