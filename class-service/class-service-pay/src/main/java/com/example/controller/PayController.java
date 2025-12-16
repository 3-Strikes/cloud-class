package com.example.controller;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.domain.PayOrder;
import com.example.dto.ApplyPayDTO;
import com.example.enums.OrderStatus;
import com.example.exceptions.BusinessException;
import com.example.result.JSONResult;
import com.example.service.AlipayService;
import com.example.service.PayOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("pay")
public class PayController {

    @Autowired
    public PayOrderService payOrderService;

    @Autowired
    private AlipayService alipayService;


    //退款功能
    // PayController.java 中新增退款接口
    @PostMapping("refund/{orderNo}")
    public JSONResult refund(@PathVariable String orderNo) {
        try {
            payOrderService.handleRefund(orderNo);
            return JSONResult.success("退款成功");
        } catch (BusinessException e) {
            return JSONResult.error(e.getMessage());
        } catch (Exception e) {
            return JSONResult.error("系统异常，退款失败");
        }
    }


    @GetMapping("checkPayOrder/{orderNo}")
    public JSONResult checkPayOrder(@PathVariable String orderNo){
        PayOrder payOrder = payOrderService.getOne(Wrappers.lambdaQuery(PayOrder.class).eq(PayOrder::getOrderNo, orderNo).eq(PayOrder::getPayStatus, OrderStatus.TO_PAY.getCode()));
        //PayOrder payOrder = payOrderService.getOne(Wrappers.lambdaQuery(PayOrder.class).eq(PayOrder::getOrderNo, orderNo).eq(PayOrder::getPayStatus, OrderStatus.TO_PAY.getCode()));

        if(payOrder != null) return JSONResult.success();
        return JSONResult.error();
    }

    @PostMapping("apply")
    public JSONResult apply(@RequestBody @Valid ApplyPayDTO payDTO){
        Integer payType = payDTO.getPayType();
        PayOrder one = payOrderService.getOne(Wrappers.lambdaQuery(PayOrder.class).eq(PayOrder::getOrderNo, payDTO.getOrderNo()));

        if(payType==1){
            String fromHtmlStr = alipayService.apply(one, payDTO.getCallUrl());
            return JSONResult.success(fromHtmlStr);
        }else if(payType==2){
            //微信
            return JSONResult.success();
        }else{
            return JSONResult.error("请选择支付方式");
        }
    }
}
