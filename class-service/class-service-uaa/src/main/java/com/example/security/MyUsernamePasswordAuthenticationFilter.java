package com.example.security;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.example.domain.Login;
import com.example.service.LoginService;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

public class MyUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    @Autowired
    private LoginService loginService;

    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = new AntPathRequestMatcher("/login", "POST");
    public MyUsernamePasswordAuthenticationFilter() {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        ServletInputStream inputStream = request.getInputStream();
        Login clientLoginParam = JSON.parseObject(inputStream, Login.class);
        if(clientLoginParam==null)throw new UsernameNotFoundException("参数不能为空");
        if(StrUtil.isEmpty(clientLoginParam.getUsername()))throw new UsernameNotFoundException("用户名不能为空");
        if(StrUtil.isEmpty(clientLoginParam.getPassword()))throw new UsernameNotFoundException("密码不能为空");
        if(null==clientLoginParam.getType())throw new UsernameNotFoundException("type类型不能为空");


        //封装成一个未认证的Authentication
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(JSON.toJSONString(clientLoginParam), clientLoginParam.getPassword());
        authRequest.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return this.getAuthenticationManager().authenticate(authRequest);
    }
}
