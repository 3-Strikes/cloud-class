package com.example.service;

import com.example.domain.PayOrder;

import java.util.Map;

public interface AlipayService {
    void cancelOrder(String orderNo);

    boolean rsaCheck(Map<String, String> map);

    String apply(PayOrder one, String returnUrl );
}
