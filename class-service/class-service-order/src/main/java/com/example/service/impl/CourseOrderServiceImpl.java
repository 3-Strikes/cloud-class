package com.example.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.CourseServiceAPI;
import com.example.cache.CacheService;
import com.example.constant.CacheKeys;
import com.example.constant.Constants;
import com.example.domain.*;
import com.example.dto.CourseOrderDTO;
import com.example.dto.OrderInfoDTO;
import com.example.enums.OrderStatus;
import com.example.mapper.CourseOrderMapper;
import com.example.result.JSONResult;
import com.example.service.CourseOrderItemService;
import com.example.service.CourseOrderService;
import com.example.vo.CourseOrderVO;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author fyt
 * @since 2025-12-13
 */
@Service
public class CourseOrderServiceImpl extends ServiceImpl<CourseOrderMapper, CourseOrder> implements CourseOrderService {
    @Autowired
    private CacheService cacheService;

    @Autowired
    private CourseOrderItemService courseOrderItemService;

    @Autowired
    private CourseServiceAPI courseServiceAPI;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void checkRepeatSubmit(String token, String loginUserId, String courseIds) {
        String key= CacheKeys.REPEAT_SUBMIT_TOKEN+loginUserId+":"+courseIds;
        Object o = cacheService.get(key);
        if(o==null){
            throw new RuntimeException("请勿重复提交");
        }
        //必须删除token，否则下次提交会通过
        cacheService.del(key);
    }

    @Override
    public String placeOrder(String loginUserId,CourseOrderDTO courseOrderDTO) {
        //生成课程订单
        List<Long> courseIds = courseOrderDTO.getCourseIds();
        Integer payType = courseOrderDTO.getPayType();

        //查询出课程对象集合，远程调用课程服务查询课程集合
        JSONResult<CourseOrderVO> info = courseServiceAPI.info(courseIds);
        CourseOrderVO data = info.getData();
        Double totalAmount = data.getTotalAmount();
        List<CourseOrderVO.CourseAndMarket> courseInfos = data.getCourseInfos();
        Date current=new Date();
        String orderId=IdUtil.getSnowflakeNextIdStr();
        String orderNo=IdUtil.getSnowflakeNextIdStr();

        List<CourseOrderItem> items=new ArrayList<>();

        StringBuffer sb=new StringBuffer();
        for (CourseOrderVO.CourseAndMarket courseInfo : courseInfos) {
            Course course = courseInfo.getCourse();
            CourseMarket courseMarket = courseInfo.getCourseMarket();
            sb.append(course.getName()+",");
            CourseOrderItem item=new CourseOrderItem();
            item.setOrderId(orderId);
            item.setOrderNo(orderNo);
            item.setAmount(courseMarket.getPrice());
            item.setCreateTime( current);
            item.setUpdateTime(current);
            item.setCourseId(course.getId());
            item.setCourseName(course.getName());
            item.setCoursePic(course.getPic());
            item.setCount(1);
            items.add(item);
        }

        CourseOrder courseOrder=new CourseOrder();
        courseOrder.setId(orderId);
        courseOrder.setCreateTime(current);
        courseOrder.setUpdateTime(current);
        courseOrder.setOrderNo(orderNo);
        courseOrder.setTotalAmount(totalAmount);
        courseOrder.setTotalCount(1);
        courseOrder.setStatusOrder(OrderStatus.TO_PAY.getCode());
        courseOrder.setUserId(Long.valueOf(loginUserId));
        courseOrder.setTitle("购买课程["+sb.toString()+"],总金额["+totalAmount+"]元");
        courseOrder.setPayType(payType);

        //发送支付单创建消息，与订单创建保持原子性（事务消息）（RocketMQTemplate.sendTrans()  +  事务监听器（执行本地事务，检测本地事务方法））
        //消息内容（支付单对象） ，由于支付服务订阅消息后，要创建支付单，所以消息内容中要包含支付单对象
        PayOrder payOrder=generatePayOrder(courseOrder);
        courseOrder.setItems(items);

        String topic= Constants.PAY_ORDER_TOPIC+":"+Constants.COURSE_ORDER_TAGS;
        rocketMQTemplate.sendMessageInTransaction(topic, MessageBuilder.withPayload(payOrder).build(), courseOrder);

        //发送延时消息，自动取消未支付订单
        String topicTag= Constants.CHECK_ORDER_STATUS_TOPIC+":"+Constants.COURSE_ORDER_STATUS_TAGS;
        rocketMQTemplate.syncSend(topicTag,MessageBuilder.withPayload(orderNo).build(), 3000, 3);//等级3（10s） 等级16（30m）


        return orderNo;
    }

    private PayOrder generatePayOrder(CourseOrder courseOrder) {
        PayOrder result=new PayOrder();
        result.setOrderNo(courseOrder.getOrderNo());
        result.setAmount(courseOrder.getTotalAmount());
        result.setPayStatus(OrderStatus.TO_PAY.getCode());
        result.setUserId(courseOrder.getUserId());
        result.setPayType(courseOrder.getPayType());
        result.setSubject(courseOrder.getTitle());
        result.setRelationId(0L);//0：课程订单
        return result;

    }

    /**
     * 根据订单编号，查询订单的购买人id，订单关联的课程id的集合
     * @param orderNo
     * @return
     */
    @Override
    public OrderInfoDTO getOrderInfo(String orderNo) {
        CourseOrder one = this.getOne(Wrappers.lambdaQuery(CourseOrder.class).eq(CourseOrder::getOrderNo, orderNo));
        Long userId = one.getUserId();
        List<CourseOrderItem> list = courseOrderItemService.list(Wrappers.lambdaQuery(CourseOrderItem.class).eq(CourseOrderItem::getOrderNo, orderNo));
        List<Long> courseIds = list.stream().map(courseOrderItem -> courseOrderItem.getCourseId()).collect(Collectors.toList());

        OrderInfoDTO result=new OrderInfoDTO();
        result.setUserId(userId);
        result.setCourseIds(courseIds);

        return result;
    }
}
