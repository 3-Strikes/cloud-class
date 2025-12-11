package com.example.mq;

import com.example.constant.Constants;
import com.example.domain.MediaFile;
import com.example.service.MediaFileService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(topic = Constants.PUSH_MEDIA_TOPIC,selectorExpression = Constants.PUSH_MEDIA_TAGS,consumerGroup = "media-pushsrs-consumer")
public class MediaPushSrsMsgConsumer implements RocketMQListener<MediaFile> {

    @Autowired
    private MediaFileService mediaFileService;
    @Override
    public void onMessage(MediaFile mediaFile) {
        //推流
        mediaFileService.handleFile2m3u8(mediaFile);
    }
}
