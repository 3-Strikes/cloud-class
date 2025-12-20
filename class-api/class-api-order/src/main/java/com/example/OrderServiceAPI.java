package com.example;

import com.example.domain.CourseOrder;
import com.example.dto.OrderInfoDTO;
import com.example.result.JSONResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("service-order")
public interface OrderServiceAPI {
    @GetMapping("courseOrder/getOrderInfoByOrderNo/{orderNo}")
     JSONResult<OrderInfoDTO> getOrderInfo(@PathVariable String orderNo);

    @GetMapping("/courseOrder/getByOrderNo/{orderNo}")
    CourseOrder getByOrderNo(@PathVariable("orderNo") String orderNo);
}
