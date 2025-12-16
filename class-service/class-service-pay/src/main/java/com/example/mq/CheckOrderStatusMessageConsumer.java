package com.example.mq;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.constant.Constants;
import com.example.domain.PayOrder;
import com.example.enums.OrderStatus;
import com.example.service.AlipayService;
import com.example.service.PayOrderService;
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
        consumerGroup ="order-service-check-status",
        messageModel = MessageModel.BROADCASTING )
public class CheckOrderStatusMessageConsumer implements RocketMQListener<String> {

    @Autowired
    private PayOrderService payOrderService;

    @Autowired
    private AlipayService alipayService;
    @Override
    public void onMessage(String orderNo) {
        //先查询订单
        PayOrder one = payOrderService.getOne(Wrappers.lambdaQuery(PayOrder.class).eq(PayOrder::getOrderNo, orderNo));
        if(one==null)return;
        Integer statusOrder = one.getPayStatus();
        //判断订单付款状态==待支付。取消订单
        if(statusOrder== OrderStatus.TO_PAY.getCode()){
            payOrderService.update(Wrappers.lambdaUpdate(PayOrder.class).set(PayOrder::getPayStatus, OrderStatus.TIMEOUT_CANCEL.getCode()).eq(PayOrder::getOrderNo, orderNo));
        }

        //通过支付宝关闭订单
        alipayService.cancelOrder(orderNo);
    }
}
