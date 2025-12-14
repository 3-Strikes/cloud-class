package com.example.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.CourseServiceAPI;
import com.example.cache.CacheService;
import com.example.constant.CacheKeys;
import com.example.domain.Course;
import com.example.domain.CourseMarket;
import com.example.domain.CourseOrder;
import com.example.domain.CourseOrderItem;
import com.example.dto.CourseOrderDTO;
import com.example.enums.OrderStatus;
import com.example.mapper.CourseOrderMapper;
import com.example.result.JSONResult;
import com.example.service.CourseOrderItemService;
import com.example.service.CourseOrderService;
import com.example.vo.CourseOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    @Transactional(rollbackFor = Exception.class)
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
        courseOrder.setTotalAmount(new BigDecimal(totalAmount));
        courseOrder.setTotalCount(1);
        courseOrder.setStatusOrder(OrderStatus.TO_PAY.getCode());
        courseOrder.setUserId(Long.valueOf(loginUserId));
        courseOrder.setTitle("购买课程["+sb.toString()+"],总金额["+totalAmount+"]元");
        courseOrder.setPayType(payType);

        this.save(courseOrder);
        courseOrderItemService.saveBatch(items);



        return orderNo;
    }


}
