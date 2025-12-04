package com.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "测试接口")
@RestController
@RequestMapping("/hello")
public class HelloController {

    @GetMapping("/{name}")
    public String hello(@PathVariable String name) {
        return "hello " + name;
    }

}
