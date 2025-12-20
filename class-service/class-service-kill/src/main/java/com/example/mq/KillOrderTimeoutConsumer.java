package com.example.mq;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.cache.CacheService;
import com.example.constant.CacheKeys;
import com.example.constant.Constants;
import com.example.domain.CourseOrder;
import com.example.domain.CourseOrderItem;
import com.example.enums.OrderStatus;
import com.example.service.CourseOrderItemService;
import com.example.service.CourseOrderService;
import com.example.service.KillCourseService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
@RocketMQMessageListener(
        topic = Constants.CHECK_ORDER_STATUS_TOPIC,
        selectorExpression = Constants.KILL_ORDER_STATUS_TAGS,
        consumerGroup = "kill-order-status-consumer-group"
)
public class KillOrderTimeoutConsumer implements RocketMQListener<Map<String, String>> { // 注意：消息类型从String改为Map，适配原Listener的消息格式
    @Autowired
    private CourseOrderService courseOrderService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private CourseOrderItemService courseOrderItemService;
    @Autowired
    private CacheService cacheService; // 保留CacheService，复用已有方法

    @Override
    public void onMessage(Map<String, String> message) { // 接收Map类型消息，获取orderNo、actId、courseId
        String orderNo = message.get("orderNo");
        String actId = message.get("actId");
        String courseId = message.get("courseId");

        // 1. 查询订单状态（复用原Listener的校验逻辑）
        CourseOrder order = courseOrderService.getOne(
                Wrappers.lambdaQuery(CourseOrder.class).eq(CourseOrder::getOrderNo, orderNo)
        );
        if (order == null || order.getStatusOrder() != OrderStatus.TO_PAY.getCode()) {
            return; // 订单已处理，直接返回
        }

        // 2. 取消订单（保留状态更新逻辑）
        order.setStatusOrder(OrderStatus.TIMEOUT_CANCEL.getCode());
        order.setUpdateTime(new Date());
        courseOrderService.updateById(order);

        // 3. 恢复库存（优先使用Lua脚本保证原子性，同时兼容actId和courseId参数）
        String stockKey = CacheKeys.KILL_ACTIVITY_COURSE_COUNT + actId + ":" + courseId;
        String lua = "local stock_key = KEYS[1];" +
                "redis.pcall('INCRBY', stock_key, 1);" +
                "return 1;";
        RedisScript<Long> script = RedisScript.of(lua, Long.class);
        redisTemplate.execute(script, Arrays.asList(stockKey));

        // 4. 移除用户秒杀标记（补充原Listener缺失的逻辑）
        String userKillKey = CacheKeys.KILL_USER_COURSE + actId + ":" + courseId;
        redisTemplate.opsForSet().remove(userKillKey, order.getUserId().toString());
    }
}


//// 辅助方法：从订单标题解析活动ID（实际应在订单表预留activity_id字段）
//    private Long parseActivityIdFromOrderTitle(String title) {
//        // 简化逻辑，实际需根据业务存储方式解析
//        return 1L;
//    }