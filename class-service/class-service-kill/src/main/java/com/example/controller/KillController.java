package com.example.controller;

import cn.hutool.core.util.IdUtil;
import com.example.constant.CacheKeys;
import com.example.domain.KillCourse;
import com.example.dto.CacheOrderDTO;
import com.example.enums.E;
import com.example.result.JSONResult;
import com.example.service.KillService;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/doKill")
public class KillController {

//    @Autowired
//    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private KillService killService;

    @Autowired
    private RedisTemplate redisTemplate;
    @GetMapping("/order/temp/{orderNo}")
    public JSONResult getTempOrder(@PathVariable String orderNo) {
        String key = CacheKeys.KILL_ORDER + orderNo;
        CacheOrderDTO tempOrder = (CacheOrderDTO) redisTemplate.opsForValue().get(key);
        if (tempOrder == null) {
            return JSONResult.error("秒杀订单不存在或已过期");
        }
        return JSONResult.success(tempOrder);
    }
    @PostMapping("killcourse")
    public JSONResult killCourse(@RequestBody KillCourse kc){
        RSemaphore killSem = redissonClient.getSemaphore("killSem");
        boolean isok = false;
        try {
            isok = killSem.tryAcquire();
            if (!isok) return JSONResult.error(E.KILL_ERROR);

            // TODO 1：检测用户是否已秒杀过该商品（1人1单）
            String loginId = "100"; // 实际从登录上下文获取
            String userKillKey = CacheKeys.KILL_USER_COURSE + kc.getActivityId() + ":" + kc.getCourseId();
            // 检查用户是否在已秒杀集合中
//            Boolean isMember = redisTemplate.opsForSet().isMember(userKillKey, loginId);
//            if (Boolean.TRUE.equals(isMember)) {
//                return JSONResult.error("您已秒杀过该商品，请勿重复参与");
//            }

            // 继续生成临时订单
            String orderNo = killService.kill(kc);
            return JSONResult.success(orderNo);
        } catch (Exception e) {
            return JSONResult.error(E.KILL_ERROR);
        } finally {
            if (isok) {
                killSem.release();
            }
        }
    }



//    private AtomicInteger count = new AtomicInteger(0);
//
//
//
//    /**
//     * sentinel的qps进行的时间段内并发达到阈值，进行限流。100ms，1000个并发
//     * 使用lua脚本，把多个命令打包执行，保持多命令之间的原子性
//     *
//     * @param courseId
//     * @return
//     */
//    @GetMapping("kill/{courseId}")
//    public String kill(@PathVariable String courseId) {
//
//        RSemaphore killSem = redissonClient.getSemaphore("killSem");
//        boolean b = killSem.tryAcquire();//获取许可
//        if(!b) return "kill fail";
//        try{
//            System.out.println("进来了-"+count.addAndGet(1));
//            String key="ymcc:kill:courseId:"+courseId;
//
//            String lua="local seckill_num = tonumber(ARGV[1]);" +
//                    "local stock_key = KEYS[1];" +
//                    "local current_stock = redis.pcall('GET', stock_key) or 0;" +
//                    "current_stock = tonumber(current_stock);" +
//                    "if current_stock < seckill_num then return -1; end;" +
//                    "local remain_stock = redis.pcall('DECRBY', stock_key, seckill_num);" +
//                    "return remain_stock;";
//
//            RedisScript<Long> objectRedisScript = RedisScript.of(lua, Long.class);
//
//            Long execute = (Long)redisTemplate.execute(objectRedisScript, Arrays.asList(key), 1);
//            if(execute<0) return  "kill fail";
//
//            String orderKey="ymcc:killOrder:"+courseId;
//            redisTemplate.opsForList().leftPush(orderKey, IdUtil.getSnowflakeNextIdStr());
//            return "kill suc";
//        }catch (Exception e){
//            return "kill fail";
//        }finally {
//            if(b){
//                killSem.release();
//            }
//        }
//    }
}
