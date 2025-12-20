package com.example.controller;

import cn.hutool.core.date.DateUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.example.OrderServiceAPI;
import com.example.constant.CacheKeys;
import com.example.constant.Constants;
import com.example.domain.CourseOrder;
import com.example.domain.PayFlow;
import com.example.domain.PayOrder;
import com.example.enums.OrderStatus;
import com.example.service.AlipayService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("alipay")
public class AliPayCallBackController {

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;


    // 新增：注入订单服务Feign（用于查询订单是否为秒杀订单）
    @Autowired
    private OrderServiceAPI courseOrderFeignClient;

    // 新增：注入RedisTemplate（用于操作秒杀相关缓存）
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate; // 与配置类保持一致

    @PostMapping("callback")
    public String callback(HttpServletRequest request) throws AlipayApiException {
        String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAz3LI/LTSys1Y94jJbO39J/5hcHwtKz8fgt85NXWzO18lIROz+/HkfWptC1sxd7t56aSmCD9XdtOf5BNID3RZPqjjOAt5/p9RCFVc1576IBeHkB6xJtdOOqkPzadIfqP2bS4uhTeVvGw7ZtujBwmT7B7nHyneoumBMqVCDjw4JcvmjG+D3lHVomkWcpH7B/7hu+e2awG7GGtbo4q5O08eEZoH0n4NxAKFeF5WtR4DXUTDdR9ORHzP2A2oqYvBtFsLgVe8QA3/6AYLkKI3gJgKVAHpteftGL2UT8+pwsegtVkyLejUDeLDM02fy7qT6pv2SlV0pB0zhoJW5qV+w1JfJwIDAQAB";
        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<Map.Entry<String, String[]>> entries = parameterMap.entrySet();

        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, String[]> entry : entries) {
            String key = entry.getKey();
            String[] value = entry.getValue();
            map.put(key, value[0]);
        }

        // 1. 签名验证
        boolean signVerified = AlipaySignature.rsaCheckV1(map, alipayPublicKey, "UTF-8", "RSA2");
        if (!signVerified) {
            System.out.println("签名验证失败！");
            return "error";
        }
        System.out.println("签名验证成功！");

        // 2. 校验并解析支付结果
        boolean isok = alipayService.rsaCheck(map);
        if (!isok) {
            return "error";
        }

        String orderNo = map.get("out_trade_no");
        String tradeStatus = map.get("trade_status");
        String totalAmount = map.get("total_amount");
        String notifyTime = map.get("notify_time");
        String subject = map.get("subject");
        OrderStatus orderStatus = null;

        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            orderStatus = OrderStatus.PAY_SUC;
        } else if ("TRADE_CLOSED".equals(tradeStatus)) {
            orderStatus = OrderStatus.TIMEOUT_CANCEL;
        }

        // 3. 构建支付单和流水信息
        PayOrder msg = new PayOrder();
        PayOrder payOrder = new PayOrder();
        payOrder.setOrderNo(orderNo);
        payOrder.setPayStatus(orderStatus.getCode());
        BeanUtils.copyProperties(payOrder, msg);

        PayFlow flow = new PayFlow();
        flow.setNotifyTime(DateUtil.parse(notifyTime, "yyyy-MM-dd HH:mm:ss"));
        flow.setSubject(subject);
        flow.setOutTradeNo(orderNo);
        flow.setTotalAmount(new BigDecimal(totalAmount));
        flow.setTradeStatus(tradeStatus);
        payOrder.setPayFlow(flow);

        // 4. 秒杀订单特有处理（核心修改）
        if (orderStatus == OrderStatus.PAY_SUC) {
            // 4.1 查询订单详情，判断是否为秒杀订单
            CourseOrder courseOrder = courseOrderFeignClient.getByOrderNo(orderNo); // 调用Feign查询订单
            if (courseOrder != null && courseOrder.getIsKill() == 1) { // 假设CourseOrder有isKill字段标识秒杀订单
                // 4.2 获取秒杀相关参数（活动ID、课程ID、用户ID）
                Long actId = courseOrder.getActId(); // 假设订单表存储活动ID
                Long courseId = Long.valueOf(courseOrder.getId());
                Long userId = courseOrder.getUserId();

                // 4.3 移除用户的「已秒杀」标记（避免重复秒杀）
                String userKillKey = CacheKeys.KILL_USER_COURSE + actId + ":" + courseId;
                redisTemplate.opsForSet().remove(userKillKey, userId.toString());

                // 4.4 （可选）向秒杀服务发送支付成功消息，用于后续业务（如统计秒杀成功率）
                String killPaySuccessTopic = Constants.KILL_PAY_SUCCESS_TOPIC; // 新增主题：秒杀支付成功
                rocketMQTemplate.convertAndSend(killPaySuccessTopic, orderNo);
            }
        }

        // 5. 发送事务消息同步订单状态（原有逻辑保留）
        String topicTag = Constants.PAY_ORDER_TOPIC + ":" + Constants.COURSE_ORDER_STATUS_TAGS;
        TransactionSendResult transactionSendResult = rocketMQTemplate.sendMessageInTransaction(
                topicTag,
                MessageBuilder.withPayload(msg).build(),
                payOrder
        );

        return transactionSendResult.getSendStatus() == SendStatus.SEND_OK ? "success" : "error";
    }

    /**
     * 1. 在进行异步通知交互时，如果支付宝收到的应答不是 success ，支付宝会认为通知失败，会通过一定的策略定期重新发起通知。通知的间隔频率为：4m、10m、10m、1h、2h、6h、15h。
     * 2. 商家设置的异步地址（notify_url）需保证无任何字符，如空格、HTML 标签，且不能重定向。（如果重定向，支付宝会收不到 success 字符，会被支付宝服务器判定为该页面程序运行出现异常，而重发处理结果通知）
     * 3. 支付宝是用 POST 方式发送通知信息，商户获取参数的方式如下：request.Form("out_trade_no")、$_POST['out_trade_no']。
     * 4. 支付宝针对同一条异步通知重试时，异步通知参数中的 notify_id 是不变的。
     *
     */
//    @PostMapping("callback")
//    public String callback(HttpServletRequest request) throws AlipayApiException {
//        String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAz3LI/LTSys1Y94jJbO39J/5hcHwtKz8fgt85NXWzO18lIROz+/HkfWptC1sxd7t56aSmCD9XdtOf5BNID3RZPqjjOAt5/p9RCFVc1576IBeHkB6xJtdOOqkPzadIfqP2bS4uhTeVvGw7ZtujBwmT7B7nHyneoumBMqVCDjw4JcvmjG+D3lHVomkWcpH7B/7hu+e2awG7GGtbo4q5O08eEZoH0n4NxAKFeF5WtR4DXUTDdR9ORHzP2A2oqYvBtFsLgVe8QA3/6AYLkKI3gJgKVAHpteftGL2UT8+pwsegtVkyLejUDeLDM02fy7qT6pv2SlV0pB0zhoJW5qV+w1JfJwIDAQAB";
//        Map<String, String[]> parameterMap = request.getParameterMap();
//        Set<Map.Entry<String, String[]>> entries = parameterMap.entrySet();
//
//        Map<String, String> map=new HashMap<>();
//        for (Map.Entry<String, String[]> entry : entries) {
//            String key = entry.getKey();
//            String[] value = entry.getValue();
//            map.put(key,value[0]);
//        }
//        System.out.println(map);
//        System.out.println("-------------------------------");
//        boolean signVerified = AlipaySignature.rsaCheckV1(map, alipayPublicKey, "UTF-8","RSA2"); //调用SDK验证签名
//        if(signVerified){
//            System.out.println("签名验证成功！");
//            System.out.println(map);
//        }else{
//            System.out.println("签名验证失败！");
//        }
//
//
//        boolean isok = alipayService.rsaCheck(map);
//
//        if(isok){
//            String orderNo = map.get("out_trade_no");
//            String tradeStatus = map.get("trade_status");
//            String totalAmount = map.get("total_amount");
//            String notifyTime = map.get("notify_time");
//            String subject = map.get("subject");
//            OrderStatus orderStatus = null;
//
//            if("TRADE_SUCCESS".equals(tradeStatus)){
//                orderStatus = OrderStatus.PAY_SUC;
//            } else if ("TRADE_CLOSED".equals(tradeStatus)) {
//                orderStatus = OrderStatus.TIMEOUT_CANCEL;
//            }
//
//            PayOrder msg=new PayOrder();
//            PayOrder payOrder=new PayOrder();
//            payOrder.setOrderNo(orderNo);
//            payOrder.setPayStatus(orderStatus.getCode());
//            BeanUtils.copyProperties(payOrder,msg);
//
//            PayFlow flow=new PayFlow();
//            flow.setNotifyTime(DateUtil.parse(notifyTime,"yyyy-MM-dd HH:mm:ss"));
//            flow.setSubject(subject);
//            flow.setOutTradeNo(orderNo);
//            flow.setTotalAmount(new BigDecimal(totalAmount));
//            flow.setTradeStatus(tradeStatus);
//            payOrder.setPayFlow(flow);
//
//            //通过mq事务消息发送支付单完成消息
//            //订单服务、修改课程订单状态
//            //发送课程中心、 用户购买课程记录
//            String topicTag = Constants.PAY_ORDER_TOPIC+":"+Constants.COURSE_ORDER_STATUS_TAGS;
//            TransactionSendResult transactionSendResult = rocketMQTemplate.sendMessageInTransaction(topicTag, MessageBuilder.withPayload(msg).build(), payOrder);
//            if(transactionSendResult.getSendStatus()== SendStatus.SEND_OK){
//                return "success";
//            }else {
//                return "error";
//            }
//
//        }else {
//            return "error";
//        }
//    }
}
