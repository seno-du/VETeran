package com.jjangtrio.veteran.ServerApplication.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @GetMapping("/work")
    public String test2(){
        System.out.println("Test~~~~~~~~~~~~~~~~~");
        return "test";
    }
}
