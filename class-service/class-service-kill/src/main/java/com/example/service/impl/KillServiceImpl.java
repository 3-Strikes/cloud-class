package com.example.service.impl;

import cn.hutool.core.util.IdUtil;
import com.example.constant.CacheKeys;
import com.example.constant.Constants;
import com.example.domain.KillCourse;
import com.example.dto.CacheOrderDTO;
import com.example.enums.E;
import com.example.exceptions.KillException;
import com.example.service.KillService;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Service
public class KillServiceImpl implements KillService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public String kill(KillCourse kc) {
        // 1. 从Redis获取完整的KillCourse对象（包含killPrice）
        // 缓存键：活动发布时存入的hash键（CacheKeys.KILL_ACTIVITY + 活动ID）
        String killActivityKey = CacheKeys.KILL_ACTIVITY + kc.getActivityId();
        // 从hash中获取该课程的完整信息（field为courseId）
        KillCourse cacheKillCourse = (KillCourse) redisTemplate.opsForHash().get(
                killActivityKey,
                kc.getCourseId().toString() // field是课程ID的字符串形式
        );

//        // 校验缓存中的秒杀课程是否存在
//        if (cacheKillCourse == null) {
//            throw new KillException(E.KILL_COURSE_NOT_EXIST); // 需新增该枚举值
//        }

        // 2. 库存扣减（使用缓存中的完整对象，确保后续逻辑正确）
        String lua = "local seckill_num = tonumber(ARGV[1]);" +
                "local stock_key = KEYS[1];" +
                "local current_stock = redis.pcall('GET', stock_key) or 0;" +
                "current_stock = tonumber(current_stock);" +
                "if current_stock < seckill_num then return -1; end;" +
                "local remain_stock = redis.pcall('DECRBY', stock_key, seckill_num);" +
                "return remain_stock;";

        RedisScript<Long> objectRedisScript = RedisScript.of(lua, Long.class);
        // 库存键：使用缓存中的活动ID和课程ID（与发布时一致）
        String stockKey = CacheKeys.KILL_ACTIVITY_COURSE_COUNT +
                cacheKillCourse.getActivityId() + ":" +
                cacheKillCourse.getCourseId();
        Long lastStockCount = (Long) redisTemplate.execute(objectRedisScript, Arrays.asList(stockKey), 1);
        if (lastStockCount < 0) {
            throw new KillException(E.KILL_ERROR);
        }

        // 3. 生成临时订单（使用缓存中的killPrice）
        String loginId = "100"; // 实际从登录上下文获取
        String orderNo = IdUtil.getSnowflakeNextIdStr();
        CacheOrderDTO cacheOrderDTO = new CacheOrderDTO();
        cacheOrderDTO.setActId(cacheKillCourse.getActivityId().toString());
        cacheOrderDTO.setCourseId(cacheKillCourse.getCourseId().toString());
        cacheOrderDTO.setUserId(loginId);
        cacheOrderDTO.setKillPrice(cacheKillCourse.getKillPrice()); // 从缓存对象中获取价格

        // 4. 缓存临时订单和用户秒杀记录（原逻辑保留）
        String userKillKey = CacheKeys.KILL_USER_COURSE + cacheKillCourse.getActivityId() + ":" + cacheKillCourse.getCourseId();
        redisTemplate.opsForSet().add(userKillKey, loginId);
        redisTemplate.expire(userKillKey, 30, TimeUnit.MINUTES);

        redisTemplate.opsForValue().set(
                CacheKeys.KILL_ORDER + orderNo,
                cacheOrderDTO,
                30, TimeUnit.MINUTES
        );

        // 发送延时消息（原逻辑保留）
        rocketMQTemplate.syncSend(
                Constants.CHECK_CACHE_ORDER_CONFIRM_STATUS,
                MessageBuilder.withPayload(orderNo).build(),
                3000, 5
        );

        return orderNo;
    }
}
