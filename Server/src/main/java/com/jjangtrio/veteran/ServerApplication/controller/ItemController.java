package com.jjangtrio.veteran.ServerApplication.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jjangtrio.veteran.ServerApplication.dto.ItemDTO;
import com.jjangtrio.veteran.ServerApplication.service.ItemService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @PostMapping("insert")
    public ResponseEntity<?> insertItem(@RequestBody ItemDTO dto) {

        itemService.insertItem(dto);

        return ResponseEntity.ok().build();
    }

}
