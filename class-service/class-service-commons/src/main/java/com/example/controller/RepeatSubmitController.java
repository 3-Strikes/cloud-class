package com.example.controller;

import cn.hutool.core.util.IdUtil;
import com.example.cache.CacheService;
import com.example.constant.CacheKeys;
import com.example.result.JSONResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("createToken")
public class RepeatSubmitController {

    @Autowired
    private CacheService cacheService;

    @GetMapping("{courseId}")
    public JSONResult createToken(@PathVariable String courseId) {
        //TODO 得到登录人信息
//        String loginId="100";
        String token= IdUtil.fastSimpleUUID();//创建token
        String key= CacheKeys.REPEAT_SUBMIT_TOKEN+":"+courseId;
        cacheService.setex(key,token,30l, TimeUnit.MINUTES);
        return JSONResult.success(token);
    }
}
