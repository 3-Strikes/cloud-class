package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.api.LoginServiceAPI;
import com.example.domain.Login;
import com.example.domain.User;
import com.example.domain.UserAccount;
import com.example.dto.RegisDTO;
import com.example.enums.UserType;
import com.example.mapper.UserMapper;
import com.example.result.JSONResult;
import com.example.service.UserAccountService;
import com.example.service.UserService;
import com.example.util.MD5Utils;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 会员登录账号 服务实现类
 * </p>
 *
 * @author fyt
 * @since 2025-12-05
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserAccountService accountService;
    @Autowired
    private LoginServiceAPI loginServiceAPI;

    @Override
    public Long addUaaLogin(RegisDTO regisInfo) {
        Login login = new Login();
        login.setUsername(regisInfo.getMobile());
        login.setPassword(MD5Utils.encrypt32(regisInfo.getPassword()));
        login.setType(UserType.WEB_SITE.getCode());
        login.setEnabled(true);
        login.setAccountNonExpired(true);
        login.setCredentialsNonExpired(true);
        login.setAccountNonLocked(true);
        login.setClientId(UserType.WEB_SITE.getDesc());
        JSONResult<Long> jsonResult = loginServiceAPI.saveOrUpdate(login);
        Long loginId = jsonResult.getData();
        return loginId;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Override
    public void regis(RegisDTO regisInfo) {
        //uaa服务：保存认证信息
        Long loginId=this.addUaaLogin(regisInfo);

        //user服务:保存用户
        Long userId=this.addUser(regisInfo,loginId);
        //user服务：初始化账户
        this.initAccount(regisInfo,userId);
//        int a = 1/0;
    }

    @Override
    public Long addUser(RegisDTO regisInfo,Long loginId) {
        long current=System.currentTimeMillis();
        User user = new User();
        user.setPhone(regisInfo.getMobile());
        user.setCreateTime(current);
        user.setUpdateTime(current);
        user.setNickName("小可爱");
        user.setLoginId(loginId);
        save(user);
        return user.getId();
    }

    @Override
    public void initAccount(RegisDTO regisInfo,Long userId) {
        UserAccount userAccount = new UserAccount();
        userAccount.setId(userId);
        userAccount.setFrozenAmount(0.0);
        userAccount.setUsableAmount(0.0);
        userAccount.setCreateTime(System.currentTimeMillis());
        userAccount.setUpdateTime(System.currentTimeMillis());

        accountService.save(userAccount);
    }
}
