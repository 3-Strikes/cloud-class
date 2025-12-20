package com.example.mq;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.CourseOrderItemRemoteAPI;
import com.example.CourseOrderRemoteAPI;
import com.example.domain.CourseOrder;
import com.example.domain.CourseOrderItem;
import com.example.help.CourseOrderWithItems;
import com.example.result.JSONResult;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;

import java.util.List;

// 实现本地事务监听器（确保订单与订单项保存成功后，才提交支付单消息）
@RocketMQTransactionListener
public class KillOrderTransactionListener implements RocketMQLocalTransactionListener {
    @Autowired
    private CourseOrderRemoteAPI courseOrderRemoteAPI;

    @Autowired
    private CourseOrderItemRemoteAPI courseOrderItemRemoteAPI;

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        try {
            CourseOrderWithItems orderWithItems = (CourseOrderWithItems) arg;
            CourseOrder courseOrder = orderWithItems.getCourseOrder();
            List<CourseOrderItem> items = orderWithItems.getItems();

            // 1. 远程保存订单主表
            JSONResult<Boolean> saveOrderResult = courseOrderRemoteAPI.saveOrder(courseOrder);
            if (!saveOrderResult.isSuccess() || !saveOrderResult.getData()) {
                return RocketMQLocalTransactionState.ROLLBACK;
            }

            // 2. 远程保存订单项
            JSONResult<Boolean> saveItemsResult = courseOrderItemRemoteAPI.saveBatchItems(items);
            if (!saveItemsResult.isSuccess() || !saveItemsResult.getData()) {
                return RocketMQLocalTransactionState.ROLLBACK;
            }

            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        String orderNo = (String) msg.getHeaders().get("ORDER_NO");
        JSONResult<CourseOrder> orderResult = courseOrderRemoteAPI.queryByOrderNo(orderNo);

        if (orderResult.isSuccess() && orderResult.getData() != null) {
            return RocketMQLocalTransactionState.COMMIT;
        } else {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }


}