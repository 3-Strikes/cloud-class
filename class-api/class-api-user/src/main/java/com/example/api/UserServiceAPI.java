package com.example.api;

import com.example.domain.User;
import com.example.result.JSONResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(name = "service-user")
public interface UserServiceAPI {
    @RequestMapping(value = "/user/list",method = RequestMethod.GET)
    JSONResult<List<User>> list();
}
