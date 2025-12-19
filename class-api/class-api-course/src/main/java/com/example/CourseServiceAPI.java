package com.example;

import com.example.result.JSONResult;
import com.example.vo.CourseDetailVO;
import com.example.vo.CourseOrderVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "service-course")
public interface CourseServiceAPI {
    @PostMapping("/course/info")
    JSONResult<CourseOrderVO> info(@RequestBody List<Long> courseIds);

    @PostMapping("/course/courseDetail")
    JSONResult<List<CourseDetailVO>> courseDetailData(@RequestBody List<Long> courseIds);
}
