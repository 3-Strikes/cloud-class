package com.example.mq;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.domain.CourseOrder;
import com.example.domain.CourseOrderItem;
import com.example.domain.PayOrder;
import com.example.service.CourseOrderItemService;
import com.example.service.CourseOrderService;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RocketMQTransactionListener
public class PayOrderMessageTransactionListener implements RocketMQLocalTransactionListener {
    @Autowired
    private CourseOrderItemService courseOrderItemService;
    @Autowired
    private CourseOrderService courseOrderService;

    //执行本地事务，一定要加本地事务注解
    @Transactional(rollbackFor = Exception.class)
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        Object payload = message.getPayload();
        if(payload==null)return RocketMQLocalTransactionState.ROLLBACK;
        if(o==null)return RocketMQLocalTransactionState.ROLLBACK;

        CourseOrder courseOrder= (CourseOrder) o;
        List<CourseOrderItem> items = courseOrder.getItems();
        if(items==null||items.size()==0)return RocketMQLocalTransactionState.ROLLBACK;

        courseOrderService.save(courseOrder);
        courseOrderItemService.saveBatch(items);
        return RocketMQLocalTransactionState.COMMIT;
    }

    //检查本地事务,兜底
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        Object payload = message.getPayload();
        if(payload==null)return RocketMQLocalTransactionState.ROLLBACK;
        PayOrder payOrder= (PayOrder) payload;
        String orderNo = payOrder.getOrderNo();
        CourseOrder one = courseOrderService.getOne(Wrappers.lambdaQuery(CourseOrder.class).eq(CourseOrder::getOrderNo, orderNo));
        if(one!=null)return RocketMQLocalTransactionState.COMMIT;

        return RocketMQLocalTransactionState.ROLLBACK;
    }
}
