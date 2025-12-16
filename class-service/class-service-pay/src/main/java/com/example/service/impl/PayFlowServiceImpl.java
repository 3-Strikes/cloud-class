package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.domain.PayFlow;
import com.example.enums.OrderStatus;
import com.example.mapper.PayFlowMapper;
import com.example.service.PayFlowService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author fyt
 * @since 2025-12-13
 */
@Service
public class PayFlowServiceImpl extends ServiceImpl<PayFlowMapper, PayFlow> implements PayFlowService {

    @Override
    public boolean updatePayOrderStatus(String orderNo, Integer tradeStatus) {
        int update = baseMapper.updateStatus(orderNo, tradeStatus, OrderStatus.TO_PAY.getCode());
        return update==1;
    }
}
