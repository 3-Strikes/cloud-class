package com.example.utils;

import com.alibaba.fastjson2.JSON;
import com.example.domain.Login;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class LoginUser {
    public static Login getLoginUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Login o = JSON.parseObject(name, Login.class);
        return o;
    }

    public static Long getLoginUserId(){
        Long id = getLoginUser().getId();
        return id;
    }
}
