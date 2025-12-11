package com.example.mq;

import cn.hutool.core.util.StrUtil;
import com.example.api.UserServiceAPI;
import com.example.constant.Constants;
import com.example.domain.MessageSms;
import com.example.domain.User;
import com.example.result.JSONResult;
import com.example.service.MessageSmsService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RocketMQMessageListener(topic = Constants.PUBLISH_COURSE_TOPIC,selectorExpression = Constants.SMS_TAGS,consumerGroup = "sms-msg-consumer")
public class PublishCourseSmsMessageListener implements RocketMQListener<MessageSms> {
    @Autowired
    private UserServiceAPI userServiceAPI;

    @Autowired
    private MessageSmsService smsService;
    @Override
    public void onMessage(MessageSms smsMsg) {
        //查询系统注册用户
        JSONResult<List<User>> list = userServiceAPI.list();
        List<User> data = list.getData();
        List<MessageSms> stations=new ArrayList<>();
        for (User u : data) {
            if(StrUtil.isEmpty(u.getPhone()))continue;

            MessageSms sms=new MessageSms();
            BeanUtils.copyProperties(smsMsg,sms);
            sms.setUserId(u.getId());
            stations.add(sms);
//            SendSms.sendPublishMsm(u.getPhone(),smsMsg.getContent());
        }
        smsService.saveBatch(stations);
    }
}
