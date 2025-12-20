package com.example.config;

import jakarta.annotation.PostConstruct;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitKillSemaphore {

    @Autowired
    private RedissonClient redissonClient;

    @PostConstruct
    public void init(){
        //初始化信号量
        RSemaphore killSem = redissonClient.getSemaphore("killSem");
        killSem.trySetPermits(100);//初始化信号量。应该在服务器启动时执行一次
    }
}
