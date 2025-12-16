package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.domain.PayOrder;
import com.example.enums.OrderStatus;
import com.example.exceptions.BusinessException;
import com.example.mapper.PayOrderMapper;
import com.example.service.AlipayService;
import com.example.service.PayOrderService;
import com.example.vo.RefundResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author fyt
 * @since 2025-12-13
 */
@Service
public class PayOrderServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements PayOrderService {


    @Autowired
    private AlipayService alipayService;
    @Override
    public boolean updatePayOrderStatus(String orderNo, Integer tradeStatus) {
        int update = baseMapper.updateStatus(orderNo, tradeStatus, OrderStatus.TO_PAY.getCode());
        return update==1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleRefund(String orderNo) throws Exception {
        // 1. 查询支付单
        PayOrder payOrder = this.getOne(new LambdaQueryWrapper<PayOrder>()
                .eq(PayOrder::getOrderNo, orderNo));
        if (payOrder == null) {
            throw new BusinessException("订单不存在");
        }

        // 2. 校验订单状态（只有已支付的订单才能退款）
        if (payOrder.getPayStatus() != OrderStatus.PAY_SUC.getCode()) {
            throw new BusinessException("只有已支付的订单才能退款，当前状态：" +
                    OrderStatus.values()[payOrder.getPayStatus()].getDesc());
        }

        // 3. 幂等性校验（防止重复退款）
        if (payOrder.getPayStatus() == OrderStatus.REFUND_SUC.getCode() ||
                payOrder.getPayStatus() == OrderStatus.REFUND_FAIL.getCode()) {
            throw new BusinessException("订单已处理过退款，请勿重复操作");
        }

        // 4. 调用支付宝退款接口
        RefundResultVO refundResult = alipayService.refund(payOrder);

        // 5. 更新支付单状态
        if (refundResult.isSuccess()) {
            payOrder.setPayStatus(OrderStatus.REFUND_SUC.getCode());
            // 同步更新订单服务的课程订单状态（假设订单服务有对应的更新接口）
            // courseOrderFeignClient.updateStatus(orderNo, OrderStatus.REFUND_SUC.getCode());
        } else {
            payOrder.setPayStatus(OrderStatus.REFUND_FAIL.getCode());
            throw new BusinessException("退款失败，支付宝响应：" + refundResult.getAlipayResponse());
        }
        this.updateById(payOrder);
    }
}
