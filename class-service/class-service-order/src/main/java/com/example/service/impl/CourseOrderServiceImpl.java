package com.example.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.CourseServiceAPI;
import com.example.cache.CacheService;
import com.example.constant.CacheKeys;
import com.example.constant.Constants;
import com.example.domain.*;
import com.example.dto.CacheOrderDTO;
import com.example.dto.CourseOrderDTO;
import com.example.dto.KillOrderDTO;
import com.example.dto.OrderInfoDTO;
import com.example.enums.OrderStatus;
import com.example.exceptions.BusinessException;
import com.example.help.CourseOrderWithItems;
import com.example.mapper.CourseOrderMapper;
import com.example.result.JSONResult;
import com.example.service.CourseOrderItemService;
import com.example.service.CourseOrderService;
import com.example.vo.CourseDetailVO;
import com.example.vo.CourseOrderVO;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
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

    @Autowired
    private RedisTemplate redisTemplate;

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
        // 1. 校验临时订单合法性（现有逻辑保留）
        String orderNo = courseOrderDTO.getOrderNo();
        String tempOrderKey = CacheKeys.KILL_ORDER + orderNo;
        CacheOrderDTO tempOrder = (CacheOrderDTO) redisTemplate.opsForValue().get(tempOrderKey);
        if (tempOrder == null || !tempOrder.getUserId().equals(loginUserId)) {
            throw new BusinessException("秒杀订单无效");
        }

        // 2. 组装订单数据（现有逻辑保留，补充时间和明细）
        Long courseId = Long.valueOf(tempOrder.getCourseId());
        BigDecimal killPrice = tempOrder.getKillPrice();
        Date current = new Date();
        String orderId = IdUtil.getSnowflakeNextIdStr();
        String formalOrderNo = IdUtil.getSnowflakeNextIdStr();

        List<CourseOrderItem> items = new ArrayList<>();
        CourseOrderItem item = new CourseOrderItem();
        item.setOrderId(orderId);
        item.setOrderNo(formalOrderNo);
        item.setCourseId(courseId);
        item.setAmount(killPrice.doubleValue()); // 注意类型匹配（CourseOrderItem#amount为Double）
        item.setCreateTime(current);
        item.setUpdateTime(current);
        // 补充课程名称（从缓存/Feign查询）
        item.setCourseName(getCourseName(courseId));
        items.add(item);

        CourseOrder courseOrder = new CourseOrder();
        courseOrder.setId(orderId);
        courseOrder.setOrderNo(formalOrderNo);
        courseOrder.setTotalAmount(killPrice.doubleValue());
        courseOrder.setUserId(Long.valueOf(loginUserId));
        courseOrder.setStatusOrder(OrderStatus.TO_PAY.getCode());
        courseOrder.setPayType(courseOrderDTO.getPayType());
        courseOrder.setTitle("秒杀订单：" + item.getCourseName());
        courseOrder.setCreateTime(current);
        courseOrder.setUpdateTime(current);

        // TODO 2：发送RocketMQ事务消息，保证订单与支付单原子性
        // 2.1 组装支付单
        PayOrder payOrder = new PayOrder();
        payOrder.setOrderNo(formalOrderNo);
        payOrder.setAmount(killPrice.doubleValue());
        payOrder.setPayStatus(OrderStatus.TO_PAY.getCode());
        payOrder.setUserId(Long.valueOf(loginUserId));
        payOrder.setPayType(courseOrderDTO.getPayType());
        payOrder.setSubject(courseOrder.getTitle());

        // 2.2 发送事务消息（半消息）
        String topic = Constants.PAY_ORDER_TOPIC + ":" + Constants.KILL_ORDER_TAGS;
        // 本地事务参数：课程订单（含订单项）
        CourseOrderWithItems orderWithItems = new CourseOrderWithItems(courseOrder, items);
        rocketMQTemplate.sendMessageInTransaction(
                topic,
                MessageBuilder.withPayload(payOrder).build(),
                orderWithItems // 本地事务参数
        );

        // 3. 发送支付超时延时消息（30分钟后检查支付状态）
        String delayTopic = Constants.CHECK_ORDER_STATUS_TOPIC + ":" + Constants.KILL_ORDER_STATUS_TAGS;
        rocketMQTemplate.syncSend(
                delayTopic,
                MessageBuilder.withPayload(formalOrderNo).build(),
                3000,
                16 // RocketMQ 16级延时（约30分钟）
        );

        // 4. 删除临时订单缓存（现有逻辑保留）
        redisTemplate.delete(tempOrderKey);

        return formalOrderNo;
//        //生成课程订单
//        List<Long> courseIds = courseOrderDTO.getCourseIds();
//        Integer payType = courseOrderDTO.getPayType();
//
//        //查询出课程对象集合，远程调用课程服务查询课程集合
//        JSONResult<CourseOrderVO> info = courseServiceAPI.info(courseIds);
//        CourseOrderVO data = info.getData();
//        Double totalAmount = data.getTotalAmount();
//        List<CourseOrderVO.CourseAndMarket> courseInfos = data.getCourseInfos();
//        Date current=new Date();
//        String orderId=IdUtil.getSnowflakeNextIdStr();
//        String orderNo=IdUtil.getSnowflakeNextIdStr();
//
//        List<CourseOrderItem> items=new ArrayList<>();
//
//        StringBuffer sb=new StringBuffer();
//        for (CourseOrderVO.CourseAndMarket courseInfo : courseInfos) {
//            Course course = courseInfo.getCourse();
//            CourseMarket courseMarket = courseInfo.getCourseMarket();
//            sb.append(course.getName()+",");
//            CourseOrderItem item=new CourseOrderItem();
//            item.setOrderId(orderId);
//            item.setOrderNo(orderNo);
//            item.setAmount(courseMarket.getPrice());
//            item.setCreateTime( current);
//            item.setUpdateTime(current);
//            item.setCourseId(course.getId());
//            item.setCourseName(course.getName());
//            item.setCoursePic(course.getPic());
//            item.setCount(1);
//            items.add(item);
//        }
//
//        CourseOrder courseOrder=new CourseOrder();
//        courseOrder.setId(orderId);
//        courseOrder.setCreateTime(current);
//        courseOrder.setUpdateTime(current);
//        courseOrder.setOrderNo(orderNo);
//        courseOrder.setTotalAmount(totalAmount);
//        courseOrder.setTotalCount(1);
//        courseOrder.setStatusOrder(OrderStatus.TO_PAY.getCode());
//        courseOrder.setUserId(Long.valueOf(loginUserId));
//        courseOrder.setTitle("购买课程["+sb.toString()+"],总金额["+totalAmount+"]元");
//        courseOrder.setPayType(payType);
//
//        //发送支付单创建消息，与订单创建保持原子性（事务消息）（RocketMQTemplate.sendTrans()  +  事务监听器（执行本地事务，检测本地事务方法））
//        //消息内容（支付单对象） ，由于支付服务订阅消息后，要创建支付单，所以消息内容中要包含支付单对象
//        PayOrder payOrder=generatePayOrder(courseOrder);
//        courseOrder.setItems(items);
//
//        String topic= Constants.PAY_ORDER_TOPIC+":"+Constants.COURSE_ORDER_TAGS;
//        rocketMQTemplate.sendMessageInTransaction(topic, MessageBuilder.withPayload(payOrder).build(), courseOrder);
//
//        //发送延时消息，自动取消未支付订单
//        String topicTag= Constants.CHECK_ORDER_STATUS_TOPIC+":"+Constants.COURSE_ORDER_STATUS_TAGS;
//        rocketMQTemplate.syncSend(topicTag,MessageBuilder.withPayload(orderNo).build(), 3000, 3);//等级3（10s） 等级16（30m）
//
//
//        return orderNo;
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

    @Override
    public String killPlaceOrder(String loginUserId, KillOrderDTO killOrderDTO) {
        String orderNo = killOrderDTO.getOrderNo();
        Long courseId = Long.valueOf(killOrderDTO.getCourseId());
        Integer payType = killOrderDTO.getPayType();
        CacheOrderDTO cacheOrderDTO = (CacheOrderDTO) cacheService.get(CacheKeys.KILL_ORDER + orderNo);
        String actId = cacheOrderDTO.getActId();
        KillCourse killCourse = (KillCourse) cacheService.hget(CacheKeys.KILL_ACTIVITY + actId, courseId.toString());

        Date current=new Date();
        String orderId=IdUtil.getSnowflakeNextIdStr();

        List<CourseOrderItem> items=new ArrayList<>();
        CourseOrderItem item=new CourseOrderItem();
        item.setOrderId(orderId);
        item.setOrderNo(orderNo);
        item.setAmount(killCourse.getKillPrice());
        item.setCreateTime( current);
        item.setUpdateTime(current);
        item.setCourseId(courseId);
        item.setCourseName(killCourse.getCourseName());
        item.setCoursePic(killCourse.getCoursePic());
        item.setCount(1);
        items.add(item);

//        StringBuffer sb=new StringBuffer();
//        for (CourseOrderVO.CourseAndMarket courseInfo : courseInfos) {
//            Course course = courseInfo.getCourse();
//            CourseMarket courseMarket = courseInfo.getCourseMarket();
//            sb.append(course.getName()+",");
//
//        }

        CourseOrder courseOrder=new CourseOrder();
        courseOrder.setId(orderId);
        courseOrder.setCreateTime(current);
        courseOrder.setUpdateTime(current);
        courseOrder.setOrderNo(orderNo);
        courseOrder.setTotalAmount(killCourse.getKillPrice());
        courseOrder.setTotalCount(1);
        courseOrder.setStatusOrder(OrderStatus.TO_PAY.getCode());
        courseOrder.setUserId(Long.valueOf(loginUserId));
        courseOrder.setTitle("购买课程["+killCourse.getCourseName()+"],总金额["+killCourse.getKillPrice()+"]元");
        courseOrder.setPayType(payType);

        //发送支付单创建消息，与订单创建保持原子性（事务消息）（RocketMQTemplate.sendTrans()  +  事务监听器（执行本地事务，检测本地事务方法））
        //消息内容（支付单对象） ，由于支付服务订阅消息后，要创建支付单，所以消息内容中要包含支付单对象
        PayOrder payOrder=generatePayOrder(courseOrder);
        courseOrder.setItems(items);

        String topic=Constants.PAY_ORDER_TOPIC+":"+Constants.COURSE_ORDER_TAGS;
        rocketMQTemplate.sendMessageInTransaction(topic, MessageBuilder.withPayload(payOrder).build(), courseOrder);




        //TODO 发送延时消息，自动取消未支付订单,并把库存加1
//        String topicTag= Constants.CHECK_ORDER_STATUS_TOPIC+":"+Constants.COURSE_ORDER_STATUS_TAGS;
//        rocketMQTemplate.syncSend(topicTag,MessageBuilder.withPayload(orderNo).build(), 3000, 5);//等级3（10s） 等级16（30m）
        // 发送延时消息，自动取消未支付订单并恢复库存
        String topicTag = Constants.CHECK_ORDER_STATUS_TOPIC + ":" + Constants.KILL_ORDER_STATUS_TAGS;
// 构建消息体，包含订单号、活动ID、课程ID（用于恢复库存）
        Map<String, String> messageBody = new HashMap<>();
        messageBody.put("orderNo", orderNo);
        messageBody.put("actId", actId);
        messageBody.put("courseId", courseId.toString());

// 发送延时消息（等级16对应约30分钟，可根据业务调整）
        rocketMQTemplate.syncSend(
                topicTag,
                MessageBuilder.withPayload(messageBody).build(),
                3000,
                16 // RocketMQ延时等级：16级对应30分钟（具体等级对应时间需与Broker配置一致）
        );

        return orderNo;

    }


    // 在 CourseOrderServiceImpl 中添加该方法
    private String getCourseName(Long courseId) {
        // 1. 校验课程ID
        if (courseId == null) {
            throw new BusinessException("课程ID不能为空");
        }

        // 2. 调用课程服务Feign接口，查询课程详情
        // 注意：courseDetailData接收List<Long>类型参数，此处包装为单元素列表
        JSONResult<List<CourseDetailVO>> detailResult = courseServiceAPI.courseDetailData(Collections.singletonList(courseId));

        // 3. 处理返回结果
        if (detailResult == null || !detailResult.isSuccess() || CollectionUtil.isEmpty(detailResult.getData())) {
            throw new BusinessException("获取课程信息失败");
        }

        // 4. 从CourseDetailVO中提取课程名称
        CourseDetailVO courseDetailVO = detailResult.getData().get(0);
        if (courseDetailVO == null || courseDetailVO.getCourse() == null) {
            throw new BusinessException("课程信息不完整");
        }

        return courseDetailVO.getCourse().getName();
    }
}


