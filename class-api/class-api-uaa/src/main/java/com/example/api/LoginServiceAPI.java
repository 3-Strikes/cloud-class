package com.example.api;

import com.example.domain.Login;
import com.example.result.JSONResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "service-uaa")
public interface LoginServiceAPI {
    @RequestMapping(value="/login/save",method= RequestMethod.POST)
    JSONResult<Long> saveOrUpdate(@RequestBody Login login);

}
