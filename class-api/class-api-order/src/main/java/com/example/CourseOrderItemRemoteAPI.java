package com.example;

import com.example.domain.CourseOrderItem;
import com.example.result.JSONResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

// 订单项服务远程API
@FeignClient(value = "class-service-order")
public interface CourseOrderItemRemoteAPI {

    @PostMapping("/course-order-item/save-batch")
    JSONResult<Boolean> saveBatchItems(@RequestBody List<CourseOrderItem> items);
}