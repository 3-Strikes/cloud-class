package com.example.mq;

import cn.hutool.core.util.StrUtil;
import com.example.api.UserServiceAPI;
import com.example.constant.Constants;
import com.example.domain.MessageEmail;
import com.example.domain.User;
import com.example.result.JSONResult;
import com.example.service.MessageEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RocketMQMessageListener(topic = Constants.PUBLISH_COURSE_TOPIC,selectorExpression = Constants.EMAIL_TAGS,consumerGroup = "email-msg-consumer")
public class PublishCourseEmailMessageListener implements RocketMQListener<MessageEmail> {
    @Autowired
    private UserServiceAPI userServiceAPI;

    @Autowired
    private MessageEmailService s;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MailProperties mailProperties;
    @Override
    public void onMessage(MessageEmail smsMsg) {
        //查询系统注册用户
        JSONResult<List<User>> list = userServiceAPI.list();
        List<User> data = list.getData();
        List<MessageEmail> stations=new ArrayList<>();
        for (User u : data) {
            if(StrUtil.isEmpty(u.getEmail()))continue;

            MessageEmail sms=new MessageEmail();
            BeanUtils.copyProperties(smsMsg,sms);
            sms.setUserId(u.getId());
            sms.setEmail(u.getEmail());
            stations.add(sms);

            MimeMessage msg = mailSender.createMimeMessage();
            //辅助器对象
            MimeMessageHelper h = new MimeMessageHelper(msg);
            try {
                h.setFrom(mailProperties.getUsername());//发件人，必须与yml中的username一致
                h.addTo(u.getEmail());//收件人
                h.setSubject(smsMsg.getTitle());
                h.setText(smsMsg.getContent(),true);
                mailSender.send(msg);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }
        s.saveBatch(stations);
    }
}