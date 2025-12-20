package com.example;

import com.example.domain.CourseOrder;
import com.example.result.JSONResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

// 订单服务远程API
@FeignClient(value = "class-service-order")
public interface CourseOrderRemoteAPI {

    @GetMapping("/course-order/query-by-no")
    JSONResult<CourseOrder> queryByOrderNo(@RequestParam("orderNo") String orderNo);

    @PostMapping("/course-order/update-status")
    JSONResult<Boolean> updateOrderStatus(@RequestBody CourseOrder order);

    @PostMapping("/course-order/save")
    JSONResult<Boolean> saveOrder(@RequestBody CourseOrder courseOrder);
}



