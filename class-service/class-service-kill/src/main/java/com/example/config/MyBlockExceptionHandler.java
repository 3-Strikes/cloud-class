package com.example.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.fastjson2.JSON;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 如果被流控，或熔断。抛出异常。
 * sentinel异常处理类
 */
@Component
public class MyBlockExceptionHandler implements BlockExceptionHandler {
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, BlockException e) throws Exception {
        resp.setContentType("application/json;charset=utf-8");
        String msg="服务异常";
        if(e instanceof FlowException){
            msg="运气不太好，再试一次";
        }
        Map<String,Object> map=new HashMap<>();
        map.put("code",50000);
        map.put("msg",msg);
        String jsonString = JSON.toJSONString(map);
        resp.getWriter().println(jsonString);
    }
}
