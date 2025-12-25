package com.example.config;

import com.alibaba.fastjson2.JSON;
import com.example.enums.E;
import com.example.result.JSONResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ResouceSecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
//                    .requestMatchers("/**").hasAuthority("SCOPE_course")
                        .anyRequest().authenticated())
                .oauth2ResourceServer((oauth2) -> oauth2
                        .jwt(Customizer.withDefaults())
                        .accessDeniedHandler(new AccessDeniedHandler() {
                            @Override
                            public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                                response.setContentType("application/json;charset=utf-8");
                                response.getWriter().write(JSON.toJSONString(JSONResult.error(E.NOPER)));
                            }
                        })
                        .authenticationEntryPoint(new AuthenticationEntryPoint() {
                            @Override
                            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                                if(authException instanceof InsufficientAuthenticationException){
                                    String accept=request.getHeader("accept");
                                    if(accept.contains(MediaType.TEXT_HTML_VALUE)){
                                        LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint=new LoginUrlAuthenticationEntryPoint("/login");
                                        loginUrlAuthenticationEntryPoint.commence(request,response,authException);
                                    }else{
                                        response.setContentType("application/json;charset=utf-8");
                                        response.getWriter().write(JSON.toJSONString(JSONResult.error(E.TOLOGIN)));
                                    }
                                }else{
                                    response.setContentType("application/json;charset=utf-8");
                                    response.getWriter().write(JSON.toJSONString(JSONResult.error(E.TOLOGIN)));
                                }

                            }
                        })
                );
        return http.build();
    }
    
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        // 自定义转换器：同时解析 scope（带SCOPE_前缀）和 authorities（无前缀）
        Converter<Jwt, Collection<GrantedAuthority>> customJwtGrantedAuthoritiesConverter = jwt -> {
            // 1. 解析默认的 scope 字段（添加 SCOPE_ 前缀）
            JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
            scopeConverter.setAuthorityPrefix("SCOPE_"); // 恢复scope的默认前缀
            Collection<GrantedAuthority> scopeAuthorities = scopeConverter.convert(jwt);

            // 2. 解析自定义的 authorities 字段（无前缀）
            List<String> customAuthorities = jwt.getClaim("authorities");
            Collection<GrantedAuthority> customGrantedAuthorities = new ArrayList<>();
            for (String customAuthority : customAuthorities) {
                customGrantedAuthorities.add(new SimpleGrantedAuthority(customAuthority));
            }

            // 3. 合并 scope 权限和自定义 authorities 权限
            List<GrantedAuthority> allAuthorities = new ArrayList<>();
            allAuthorities.addAll(scopeAuthorities);
            allAuthorities.addAll(customGrantedAuthorities);
            return allAuthorities;
        };

        // 绑定自定义转换器到 JwtAuthenticationConverter
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(customJwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}