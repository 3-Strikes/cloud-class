package com.example.mq;

import com.example.api.UserServiceAPI;
import com.example.constant.Constants;
import com.example.domain.MessageStation;
import com.example.domain.User;
import com.example.result.JSONResult;
import com.example.service.MessageStationService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RocketMQMessageListener(topic = Constants.PUBLISH_COURSE_TOPIC,selectorExpression = Constants.STATION_TAGS,consumerGroup = "station-msg-consumer")
public class PublishCourseStationMessageListener implements RocketMQListener<MessageStation> {
    @Autowired
    private UserServiceAPI userServiceAPI;

    @Autowired
    private MessageStationService stationService;
    @Override
    public void onMessage(MessageStation messageStation) {
        //查询系统注册用户
        JSONResult<List<User>> list = userServiceAPI.list();
        List<User> data = list.getData();
        List<MessageStation> stations=new ArrayList<>();
        for (User u : data) {
            MessageStation station=new MessageStation();
            BeanUtils.copyProperties(messageStation,station);
            station.setUserId(u.getId());
            stations.add(station);
        }
        stationService.saveBatch(stations);
    }
}
