package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

// 排除数据源自动配置
@SpringBootApplication
//(exclude = {DataSourceAutoConfiguration.class})
public class CommonsApp {
    public static void main(String[] args) {
        SpringApplication.run(CommonsApp.class, args);
    }
}