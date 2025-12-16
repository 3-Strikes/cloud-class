package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.CourseOrder;
import com.example.dto.CourseOrderDTO;
import com.example.dto.OrderInfoDTO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author fyt
 * @since 2025-12-13
 */
public interface CourseOrderService extends IService<CourseOrder> {

    String placeOrder(String loginUserId,CourseOrderDTO courseOrderDTO);

    void checkRepeatSubmit(String token, String loginUserId, String courseIds);

    OrderInfoDTO getOrderInfo(String orderNo);
}
