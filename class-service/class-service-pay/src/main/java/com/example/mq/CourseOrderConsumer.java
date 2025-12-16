package com.example.mq;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.constant.Constants;
import com.example.domain.PayOrder;
import com.example.service.PayOrderService;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 如果有重置订单，再创消费者
 * 消息模式：集群模式（只会被消费一次），广播模式（可以被多消费者重复消息）
 *
 */
@Component
@RocketMQMessageListener(topic = Constants.PAY_ORDER_TOPIC,selectorExpression = Constants.COURSE_ORDER_TAGS,consumerGroup = "course-order-consumer-group")
public class CourseOrderConsumer implements RocketMQListener<PayOrder> {

    @Autowired
    private PayOrderService payOrderService;

    /**
     * 避免消息重复（重复发送）
     * @param payOrder
     */
    @Override
    public void onMessage(PayOrder payOrder) {
        String orderNo = payOrder.getOrderNo();
        //幂等性检测
        PayOrder one = payOrderService.getOne(Wrappers.lambdaQuery(PayOrder.class).eq(PayOrder::getOrderNo, orderNo));
        if(one!=null)return;

        Date date = new Date();
        payOrder.setCreateTime(date);
        payOrder.setUpdateTime(date);

        payOrderService.save(payOrder);
    }
}
