package com.example.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.example.domain.AlipayInfo;
import com.example.domain.PayOrder;
import com.example.exceptions.BusinessException;
import com.example.service.AlipayInfoService;
import com.example.service.AlipayService;
import com.example.vo.RefundResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Map;

@Service
public class AliPayServiceImpl implements AlipayService {

    private AlipayInfo alipayInfo = null;
    private AlipayClient alipayClient = null;

    @Autowired
    private AlipayInfoService alipayInfoService;

    @Override
    public RefundResultVO refund(PayOrder payOrder) throws AlipayApiException {
        // 初始化支付宝客户端（复用已有逻辑）
        if(alipayInfo == null){
            alipayInfo = alipayInfoService.list().get(0);
            alipayClient = new DefaultAlipayClient(getAlipayConfig());
        }

        // 构造退款请求
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(payOrder.getOrderNo()); // 商户订单号
        model.setRefundAmount(String.valueOf(payOrder.getAmount())); // 退款金额（与支付金额一致）
        model.setRefundReason("用户发起退款"); // 退款原因
        request.setBizModel(model);

        // 执行退款请求
        AlipayTradeRefundResponse response = alipayClient.execute(request);
        RefundResultVO result = new RefundResultVO();
        result.setSuccess(response.isSuccess());
        result.setAlipayResponse(response.getBody()); // 保存支付宝返回的原始数据
        result.setTradeNo(response.getTradeNo()); // 支付宝交易号
        return result;
    }
    @Override
    public void cancelOrder(String orderNo) {
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        AlipayTradeCloseModel model = new AlipayTradeCloseModel();
        model.setOutTradeNo(orderNo);
        request.setBizModel(model);

        try {
            AlipayTradeCloseResponse resp = alipayClient.execute(request);
            String code = resp.getCode();
            System.out.println(orderNo+"--code:"+code+"--"+resp.getMsg());
        } catch (AlipayApiException e) {
            throw new BusinessException("支付单远程关单失败");
        }
    }

    @Override
    public boolean rsaCheck(Map<String, String> map) {
        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(map, alipayInfo.getAlipayPublicKey(), "UTF-8","RSA2"); //调用SDK验证签名
            return signVerified;
        } catch (AlipayApiException e) {
            return false;
        }
    }


    @Override
    public String apply(PayOrder one, String returnUrl) {
        if(alipayInfo==null){
            try {
                alipayInfo = alipayInfoService.list().get(0);
                alipayClient = new DefaultAlipayClient(getAlipayConfig());
            }catch (AlipayApiException e){
                throw new BusinessException("支付宝支付客户端初始化失败");
            }
        }

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(StrUtil.isNotEmpty(returnUrl)?returnUrl:alipayInfo.getReturnUrl());//支付完成后跳转的页面
        request.setNotifyUrl(alipayInfo.getNotifyUrl());//内网穿透 端口映射
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();

        //设置商户订单号
        model.setOutTradeNo(one.getOrderNo());

        // 设置订单总金额，单位为元，精确到小数点后两位
        DecimalFormat df = new DecimalFormat("0.00");
        String amount = df.format(one.getAmount());
        model.setTotalAmount(amount);

        // 设置订单标题
        model.setSubject(one.getSubject());

        // 设置产品码（固定的）
        model.setProductCode("FAST_INSTANT_TRADE_PAY");

        // 设置订单超时时间
        model.setTimeoutExpress("30m");

        request.setBizModel(model);

        AlipayTradePagePayResponse response = null;

        // 发送请求
        try {
            response = alipayClient.pageExecute(request,"POST");
        } catch (AlipayApiException e) {
            throw new BusinessException("alipay支付失败");
        }

        if(response.isSuccess()){
            return response.getBody();
        }
        throw new BusinessException("alipay支付失败");
    }

    private AlipayConfig getAlipayConfig() {
        String privateKey  = alipayInfo.getMerchantPrivateKey();
        String alipayPublicKey = alipayInfo.getAlipayPublicKey();
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl(alipayInfo.getGatewayHost());
        alipayConfig.setAppId(alipayInfo.getAppId());
        alipayConfig.setPrivateKey(privateKey);
        alipayConfig.setFormat("json");
        alipayConfig.setAlipayPublicKey(alipayPublicKey);
        alipayConfig.setCharset("UTF-8");
        alipayConfig.setSignType(alipayInfo.getSignType());
        return alipayConfig;
    }
}
