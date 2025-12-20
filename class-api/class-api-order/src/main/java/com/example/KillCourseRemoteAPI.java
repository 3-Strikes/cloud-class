package com.example;

import com.example.result.JSONResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// 秒杀课程服务远程API
@FeignClient(value = "class-service-kill")
public interface KillCourseRemoteAPI {

    @PostMapping("/kill-course/recover-stock")
    JSONResult<Boolean> recoverStock(@RequestParam("actId") String actId,
                                     @RequestParam("courseId") String courseId);
}
