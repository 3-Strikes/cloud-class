package com.example.security;

import com.example.utils.JwtTokenUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class MyTmpTokenCheckFilter extends OncePerRequestFilter {

    @Autowired
    private UaaUserDetailService userDetailsService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (!request.getRequestURI().startsWith("/oauth2")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 从请求头获取临时凭证
        String tempToken = request.getHeader("X-Temp-Token");
        if (tempToken == null) {
            tempToken = request.getParameter("tempToken");
            if (tempToken == null) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        //设置SecurityContextHolder的存储模式，MODE_INHERITABLETHREADLOCAL当前线程与子线程都可以取得上下文数据;MODE_THREADLOCAL只有当前线程才可以获取数据
        String requestUri = request.getRequestURI();
        if (requestUri.contains("/oauth2/authorize")) {
            //生成授权码filter，此过滤器的执行在子线程执行的。
            SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        } else if (requestUri.contains("/oauth2/token")) {
            SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_THREADLOCAL);
        }
        SecurityContextHolder.clearContext();


        try {
            Claims claims = JwtTokenUtils.validateToken(tempToken);
            String subject = claims.getSubject();

            UserDetails userDetails = userDetailsService.loadUserByUsername(subject);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {

        }

        filterChain.doFilter(request, response);
    }
}
