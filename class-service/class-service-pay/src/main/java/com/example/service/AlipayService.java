package com.example.service;

import com.alipay.api.AlipayApiException;
import com.example.domain.PayOrder;
import com.example.vo.RefundResultVO;

import java.util.Map;

public interface AlipayService {
    void cancelOrder(String orderNo);

    boolean rsaCheck(Map<String, String> map);

    String apply(PayOrder one, String returnUrl );

    // 新增退款方法，返回退款结果（包含是否成功、支付宝返回信息）
    RefundResultVO refund(PayOrder payOrder) throws AlipayApiException;
}
