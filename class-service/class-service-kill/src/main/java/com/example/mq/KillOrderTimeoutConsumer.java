package com.example.mq;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.CourseOrderRemoteAPI;
import com.example.KillCourseRemoteAPI;
import com.example.cache.CacheService;
import com.example.constant.CacheKeys;
import com.example.constant.Constants;
import com.example.domain.CourseOrder;
import com.example.domain.CourseOrderItem;
import com.example.enums.OrderStatus;
import com.example.result.JSONResult;
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
    private CourseOrderRemoteAPI courseOrderRemoteAPI;

    @Autowired
    private KillCourseRemoteAPI killCourseRemoteAPI;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CacheService cacheService;

    @Override
    public void onMessage(Map<String, String> message) {
        String orderNo = message.get("orderNo");
        String actId = message.get("actId");
        String courseId = message.get("courseId");

        // 1. 远程查询订单状态
        JSONResult<CourseOrder> orderResult = courseOrderRemoteAPI.queryByOrderNo(orderNo);
        if (!orderResult.isSuccess() || orderResult.getData() == null) {
            return;
        }
        CourseOrder order = orderResult.getData();
        if (order.getStatusOrder() != OrderStatus.TO_PAY.getCode()) {
            return;
        }

        // 2. 远程更新订单状态为超时取消
        order.setStatusOrder(OrderStatus.TIMEOUT_CANCEL.getCode());
        order.setUpdateTime(new Date());
        courseOrderRemoteAPI.updateOrderStatus(order);

        // 3. 远程调用恢复库存
        killCourseRemoteAPI.recoverStock(actId, courseId);

        // 4. 移除用户秒杀标记（Redis操作保留本地执行）
        String userKillKey = CacheKeys.KILL_USER_COURSE + actId + ":" + courseId;
        redisTemplate.opsForSet().remove(userKillKey, order.getUserId().toString());
    }
}


//// 辅助方法：从订单标题解析活动ID（实际应在订单表预留activity_id字段）
//    private Long parseActivityIdFromOrderTitle(String title) {
//        // 简化逻辑，实际需根据业务存储方式解析
//        return 1L;
//    }