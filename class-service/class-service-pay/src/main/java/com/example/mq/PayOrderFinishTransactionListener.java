package com.example.mq;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.domain.PayFlow;
import com.example.domain.PayOrder;
import com.example.enums.OrderStatus;
import com.example.service.PayFlowService;
import com.example.service.PayOrderService;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RocketMQTransactionListener
public class PayOrderFinishTransactionListener implements RocketMQLocalTransactionListener {
    @Autowired
    private PayOrderService payOrderService;

    @Autowired
    private PayFlowService payFlowService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        PayOrder payOrder = (PayOrder)o;
        PayFlow payFlow = payOrder.getPayFlow();
        //1. 修改支付单状态状态（乐观锁）   update pay_order set status=1 where order_no=? and status=未支付
        boolean update = payOrderService.updatePayOrderStatus(payOrder.getOrderNo(), payOrder.getPayStatus());
        if (update) {
            //2. 支付流水记录添加
            payFlowService.save(payFlow);
        }
        return RocketMQLocalTransactionState.COMMIT;
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        Object payload = message.getPayload();

        if (payload instanceof PayOrder) {
            PayOrder payOrder = (PayOrder)payload;
            String orderNo = payOrder.getOrderNo();
            //查询orderNo状态已否已支付
            PayOrder one = payOrderService.getOne(Wrappers.lambdaQuery(PayOrder.class).eq(PayOrder::getOrderNo, orderNo).eq(PayOrder::getPayStatus, OrderStatus.PAY_SUC.getCode()));
            if(one!=null)return RocketMQLocalTransactionState.COMMIT;
            else return RocketMQLocalTransactionState.ROLLBACK;
        }
        return RocketMQLocalTransactionState.UNKNOWN;
    }
}
