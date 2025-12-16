// RefundResultVO.java
package com.example.vo;

import lombok.Data;

@Data
public class RefundResultVO {
    private boolean success; // 退款是否成功
    private String alipayResponse; // 支付宝原始响应
    private String tradeNo; // 支付宝交易号
}