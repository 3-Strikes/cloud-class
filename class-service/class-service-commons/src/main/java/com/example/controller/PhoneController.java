package com.example.controller;

import com.example.cache.CacheKeys;
import com.example.cache.CacheService;
import com.example.result.JSONResult;
import com.example.util.AssertUtil;
import com.example.util.StrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("verifycode")
public class PhoneController {
    @Autowired
    private CacheService cacheService;

    @GetMapping("sendSmsCode/{phone}")
    public JSONResult sendSmsCode(@PathVariable String phone) {
        //发送验证码
        //1.校验手机号
        AssertUtil.isNotEmpty(phone, "请输入手机号");
        AssertUtil.isPhone(phone, "手机号格式错误");
        //2.从redis检测当前手机号是否已发送验证码
        String key = CacheKeys.VALI_CODE + phone;
        Object o = cacheService.get(key);
        String code = null;
        if (o != null) {
            //2.1.有的话，检测是否1分钟内。1分钟内已发送过验证码，则提示已发送过验证码，请稍后再试
            Map<String, Object> data = (Map<String, Object>) o;
            if (System.currentTimeMillis() - (long) data.get("time") < 60 * 1000) {
                return JSONResult.error("1分钟内请勿重复发送验证码");
            }
        }
        code = StrUtils.getRandomString(5);
        Map<String, Object> data = new HashMap<>();
        data.put("code", code);
        data.put("time", System.currentTimeMillis());
        cacheService.setex(key, data, 5L, TimeUnit.MINUTES);

        //异步短信发送
        System.out.println(code);
//        SendSms.sendValiCode(phone, code);
        return JSONResult.success();
    }

}
