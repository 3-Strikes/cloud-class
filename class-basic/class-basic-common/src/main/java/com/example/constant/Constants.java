package com.example.constant;

public interface Constants {
    String PUSH_MEDIA_TOPIC="media";
    String PUSH_MEDIA_TAGS="srs";

    //视频推流消息------------------end

    //课程发布推广消息--------------begin
    String PUBLISH_COURSE_TOPIC="course-publish";
    String STATION_TAGS="station";//站内信
    String SMS_TAGS="sms";//站内信
    String EMAIL_TAGS="email";//邮件
    //课程发布推广消息--------------end

    //es索引名称
    String COURSE_INDEX="course_index";

    //    支付订单消息------------------begin
    String PAY_ORDER_TOPIC="pay-order";
    String COURSE_ORDER_TAGS="course-order";//课程订单tag

    //检测订单状态信息
    String CHECK_ORDER_STATUS_TOPIC = "check_order_status";
    String COURSE_ORDER_STATUS_TAGS = "course_order";
    String COURSE_ORDER_CHECK_GROUP = "course_order_check_group";
}
