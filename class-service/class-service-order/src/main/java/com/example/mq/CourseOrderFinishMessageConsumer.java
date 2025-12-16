package com.example.mq;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.constant.Constants;
import com.example.domain.CourseOrder;
import com.example.domain.PayOrder;
import com.example.service.CourseOrderService;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(topic = Constants.PAY_ORDER_FINISH_TOPIC,
        selectorExpression = Constants.COURSE_ORDER_FINISH_TAGS,
        consumerGroup = Constants.COURSE_ORDER_FINISH_GROUP,
        messageModel = MessageModel.BROADCASTING)
public class CourseOrderFinishMessageConsumer implements RocketMQListener<PayOrder> {

    @Autowired
    private CourseOrderService courseOrderService;

    @Override
    public void onMessage(PayOrder payOrder) {
        String orderNo = payOrder.getOrderNo();
        Integer payStatus = payOrder.getPayStatus();
        courseOrderService.update(Wrappers.lambdaUpdate(CourseOrder.class).set(CourseOrder::getStatusOrder, payStatus).eq(CourseOrder::getOrderNo, orderNo));
    }
}
