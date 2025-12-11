package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

// 核心：指定Mapper接口扫描包（必须和你的Mapper接口包路径一致）

@SpringBootApplication
@EnableCaching//开启spring缓存注解
@EnableFeignClients
public class CourseApp {
    public static void main(String[] args) {
        SpringApplication.run(CourseApp.class, args);
    }
}