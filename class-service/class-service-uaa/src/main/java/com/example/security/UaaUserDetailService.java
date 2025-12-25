package com.example.security;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.domain.Login;
import com.example.domain.Permission;
import com.example.service.LoginService;
import com.example.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UaaUserDetailService implements UserDetailsService {
    @Autowired
    private LoginService loginService;

    @Autowired
    private PermissionService permissionService;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Login clientLoginParam = JSON.parseObject(username, Login.class);
        //查用户
        Login one = loginService.getOne(Wrappers.lambdaQuery(Login.class).eq(Login::getUsername, clientLoginParam.getUsername()).eq(Login::getType, clientLoginParam.getType()));
        if(one==null) throw new UsernameNotFoundException("用户不存在");

        //查权限
        List<Permission> permissions = permissionService.selectPerms(one.getId());
        List<GrantedAuthority> authorityes=new ArrayList<>();
        for (Permission permission : permissions) {
            authorityes.add(new SimpleGrantedAuthority(permission.getSn()));
        }
        //封装UserDetails对象
        Map<String,Object> basicLoginInfo=new HashMap<>();
        basicLoginInfo.put("id",one.getId());
        basicLoginInfo.put("username",one.getUsername());
        basicLoginInfo.put("type",one.getType());

        return new User(JSON.toJSONString(basicLoginInfo),one.getPassword(),one.getEnabled(),one.getAccountNonExpired(),one.getCredentialsNonExpired(),one.getAccountNonLocked(),authorityes);
    }
}
