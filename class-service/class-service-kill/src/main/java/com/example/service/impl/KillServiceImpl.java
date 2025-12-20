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
        String lua = "local seckill_num = tonumber(ARGV[1]);" +
                "local stock_key = KEYS[1];" +
                "local current_stock = redis.pcall('GET', stock_key) or 0;" +
                "current_stock = tonumber(current_stock);" +
                "if current_stock < seckill_num then return -1; end;" +
                "local remain_stock = redis.pcall('DECRBY', stock_key, seckill_num);" +
                "return remain_stock;";

        RedisScript<Long> objectRedisScript = RedisScript.of(lua, Long.class);

        String key= CacheKeys.KILL_ACTIVITY_COURSE_COUNT+kc.getActivityId()+":"+kc.getCourseId();
        Long lastStockCount = (Long) redisTemplate.execute(objectRedisScript, Arrays.asList(key), 1);
        if(lastStockCount<0) throw new KillException(E.KILL_ERROR);

//        String loginId="100";
//        //秒杀成功,生成临时订单号
//        String orderNo = IdUtil.getSnowflakeNextIdStr();
//        CacheOrderDTO cacheOrderDTO=new CacheOrderDTO();
//        cacheOrderDTO.setActId(kc.getActivityId().toString());
//        cacheOrderDTO.setCourseId(kc.getCourseId().toString());
//        cacheOrderDTO.setUserId(loginId);
//        redisTemplate.opsForValue().set(CacheKeys.KILL_ORDER+orderNo,cacheOrderDTO);//courseId,actId,userId
        String loginId = "100"; // 实际应从登录上下文获取
        String orderNo = IdUtil.getSnowflakeNextIdStr();

        // 扩展临时订单DTO，新增秒杀价格字段
        CacheOrderDTO cacheOrderDTO = new CacheOrderDTO();
        cacheOrderDTO.setActId(kc.getActivityId().toString());
        cacheOrderDTO.setCourseId(kc.getCourseId().toString());
        cacheOrderDTO.setUserId(loginId);
        // 存入秒杀价格（关键：从当前秒杀课程对象中获取）
        cacheOrderDTO.setKillPrice(kc.getKillPrice());

        // TODO 1.1：将用户ID加入「已秒杀集合」（与临时订单绑定）
        String userKillKey = CacheKeys.KILL_USER_COURSE + kc.getActivityId() + ":" + kc.getCourseId();
        redisTemplate.opsForSet().add(userKillKey, loginId);
        // 设置与临时订单相同的过期时间（如30分钟），避免无效缓存
        redisTemplate.expire(userKillKey, 30, TimeUnit.MINUTES);

        redisTemplate.opsForValue().set(
                CacheKeys.KILL_ORDER + orderNo,
                cacheOrderDTO,
                30, TimeUnit.MINUTES
        );

        // 发送延时消息检测订单确认状态（原逻辑保留）
        rocketMQTemplate.syncSend(
                Constants.CHECK_CACHE_ORDER_CONFIRM_STATUS,
                MessageBuilder.withPayload(orderNo).build(),
                3000, 5 // 5级延时（约1分钟）
        );

        return orderNo;
    }
}
