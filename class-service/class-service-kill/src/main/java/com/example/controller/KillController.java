package com.example.controller;

import cn.hutool.core.util.IdUtil;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/test")
public class KillController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;
    private AtomicInteger count = new AtomicInteger(0);



    /**
     * sentinel的qps进行的时间段内并发达到阈值，进行限流。100ms，1000个并发
     * 使用lua脚本，把多个命令打包执行，保持多命令之间的原子性
     *
     * @param courseId
     * @return
     */
    @GetMapping("kill/{courseId}")
    public String kill(@PathVariable String courseId) {

        RSemaphore killSem = redissonClient.getSemaphore("killSem");
        boolean b = killSem.tryAcquire();//获取许可
        if(!b) return "kill fail";
        try{
            System.out.println("进来了-"+count.addAndGet(1));
            String key="ymcc:kill:courseId:"+courseId;

            String lua="local seckill_num = tonumber(ARGV[1]);" +
                    "local stock_key = KEYS[1];" +
                    "local current_stock = redis.pcall('GET', stock_key) or 0;" +
                    "current_stock = tonumber(current_stock);" +
                    "if current_stock < seckill_num then return -1; end;" +
                    "local remain_stock = redis.pcall('DECRBY', stock_key, seckill_num);" +
                    "return remain_stock;";

            RedisScript<Long> objectRedisScript = RedisScript.of(lua, Long.class);

            Long execute = (Long)redisTemplate.execute(objectRedisScript, Arrays.asList(key), 1);
            if(execute<0) return  "kill fail";

            String orderKey="ymcc:killOrder:"+courseId;
            redisTemplate.opsForList().leftPush(orderKey, IdUtil.getSnowflakeNextIdStr());
            return "kill suc";
        }catch (Exception e){
            return "kill fail";
        }finally {
            if(b){
                killSem.release();
            }
        }
    }
}
