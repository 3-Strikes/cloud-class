package com.example.web.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("alipay")
public class AliPayCallBackController {

    /**
     * 1. 在进行异步通知交互时，如果支付宝收到的应答不是 success ，支付宝会认为通知失败，会通过一定的策略定期重新发起通知。通知的间隔频率为：4m、10m、10m、1h、2h、6h、15h。
     * 2. 商家设置的异步地址（notify_url）需保证无任何字符，如空格、HTML 标签，且不能重定向。（如果重定向，支付宝会收不到 success 字符，会被支付宝服务器判定为该页面程序运行出现异常，而重发处理结果通知）
     * 3. 支付宝是用 POST 方式发送通知信息，商户获取参数的方式如下：request.Form("out_trade_no")、$_POST['out_trade_no']。
     * 4. 支付宝针对同一条异步通知重试时，异步通知参数中的 notify_id 是不变的。
     *
     */
    @PostMapping("callback")
    public String callback(HttpServletRequest request) throws AlipayApiException {
        String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAz3LI/LTSys1Y94jJbO39J/5hcHwtKz8fgt85NXWzO18lIROz+/HkfWptC1sxd7t56aSmCD9XdtOf5BNID3RZPqjjOAt5/p9RCFVc1576IBeHkB6xJtdOOqkPzadIfqP2bS4uhTeVvGw7ZtujBwmT7B7nHyneoumBMqVCDjw4JcvmjG+D3lHVomkWcpH7B/7hu+e2awG7GGtbo4q5O08eEZoH0n4NxAKFeF5WtR4DXUTDdR9ORHzP2A2oqYvBtFsLgVe8QA3/6AYLkKI3gJgKVAHpteftGL2UT8+pwsegtVkyLejUDeLDM02fy7qT6pv2SlV0pB0zhoJW5qV+w1JfJwIDAQAB";
        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<Map.Entry<String, String[]>> entries = parameterMap.entrySet();

        Map<String, String> map=new HashMap<>();
        for (Map.Entry<String, String[]> entry : entries) {
            String key = entry.getKey();
            String[] value = entry.getValue();
            map.put(key,value[0]);
        }
        System.out.println(map);
        System.out.println("-------------------------------");
        boolean signVerified = AlipaySignature.rsaCheckV1(map, alipayPublicKey, "UTF-8","RSA2"); //调用SDK验证签名
        if(signVerified){
            System.out.println("签名验证成功！");
            System.out.println(map);
        }else{
            System.out.println("签名验证失败！");
        }

        //TODO 修改支付单状态状态
        //TODO 发送事务消息，修改课程订单状态，添加用户课程购买记录

        //支付完成后，阿里调用，
        //修改订单状态，（）
        return "success";
    }
}
