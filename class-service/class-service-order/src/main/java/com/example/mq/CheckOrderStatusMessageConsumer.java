package com.example.mq;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.constant.Constants;
import com.example.domain.CourseOrder;
import com.example.enums.OrderStatus;
import com.example.service.CourseOrderService;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 检测订单状态（30分钟是否已支付）   消息消费者
 */
@Component
@RocketMQMessageListener(topic = Constants.CHECK_ORDER_STATUS_TOPIC,
        selectorExpression = Constants.COURSE_ORDER_STATUS_TAGS,
        consumerGroup = Constants.COURSE_ORDER_CHECK_GROUP,
        messageModel = MessageModel.BROADCASTING )
public class CheckOrderStatusMessageConsumer implements RocketMQListener<String> {

    @Autowired
    private CourseOrderService courseOrderService;
    @Override
    public void onMessage(String orderNo) {
        //先查询订单
        CourseOrder one = courseOrderService.getOne(Wrappers.lambdaQuery(CourseOrder.class).eq(CourseOrder::getOrderNo, orderNo));
        if(one==null)return;
        Integer statusOrder = one.getStatusOrder();
        //判断订单付款状态==待支付。取消订单
        if(statusOrder== OrderStatus.TO_PAY.getCode()){
            courseOrderService.update(Wrappers.lambdaUpdate(CourseOrder.class).set(CourseOrder::getStatusOrder, OrderStatus.TIMEOUT_CANCEL.getCode()).eq(CourseOrder::getOrderNo, orderNo));
        }
    }
}
