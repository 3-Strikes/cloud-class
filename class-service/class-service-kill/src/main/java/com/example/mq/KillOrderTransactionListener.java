package com.example.mq;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.domain.CourseOrder;
import com.example.help.CourseOrderWithItems;
import com.example.service.CourseOrderItemService;
import com.example.service.CourseOrderService;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;

// 实现本地事务监听器（确保订单与订单项保存成功后，才提交支付单消息）
@RocketMQTransactionListener
public class KillOrderTransactionListener implements RocketMQLocalTransactionListener {
    @Autowired
    private CourseOrderService courseOrderService;
    @Autowired
    private CourseOrderItemService courseOrderItemService;

    // 执行本地事务
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        try {
            CourseOrderWithItems orderWithItems = (CourseOrderWithItems) arg;
            // 保存订单主表
            courseOrderService.save(orderWithItems.getCourseOrder());
            // 保存订单项
            courseOrderItemService.saveBatch(orderWithItems.getItems());
            return RocketMQLocalTransactionState.COMMIT; // 提交消息，支付服务可消费
        } catch (Exception e) {
            return RocketMQLocalTransactionState.ROLLBACK; // 回滚消息，支付服务不消费
        }
    }

    // 本地事务状态回查（解决超时未确认问题）
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        String orderNo = (String) msg.getHeaders().get("ORDER_NO"); // 从消息头获取订单号
        CourseOrder order = courseOrderService.getOne(
            Wrappers.lambdaQuery(CourseOrder.class).eq(CourseOrder::getOrderNo, orderNo)
        );
        if (order != null) {
            return RocketMQLocalTransactionState.COMMIT;
        } else {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }


}