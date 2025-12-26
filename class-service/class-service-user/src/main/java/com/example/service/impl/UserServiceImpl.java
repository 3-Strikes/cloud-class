package com.example.service.impl;

import cn.hutool.core.util.IdUtil;
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
    public String addUaaLogin(RegisDTO regisInfo, String id) {
        Login login = new Login();
        login.setId(String.valueOf(id)); // 使用传入的id，确保与User和Employee保持一致
        login.setUsername(regisInfo.getMobile());
        login.setPassword(MD5Utils.encrypt32(regisInfo.getPassword()));
        login.setType(UserType.WEB_SITE.getCode());
        login.setEnabled(true);
        login.setAccountNonExpired(true);
        login.setCredentialsNonExpired(true);
        login.setAccountNonLocked(true);
        login.setClientId(UserType.WEB_SITE.getDesc());

        //需传secret
        JSONResult<Long> jsonResult = loginServiceAPI.saveOrUpdate(login);
        String loginId = String.valueOf(jsonResult.getData());
        return loginId;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Override
    public void regis(RegisDTO regisInfo) {
        // 生成统一的id，确保t_login、t_user、t_employee三个表的id保持一致
        Long commonId = IdUtil.getSnowflakeNextId();
        
        //uaa服务：保存认证信息，使用统一的id
        String loginId = this.addUaaLogin(regisInfo, String.valueOf(commonId));

        //user服务:保存用户，使用统一的id（作为主键id，loginId字段也设置为这个值）
        String userId = this.addUser(regisInfo, String.valueOf(commonId));
        //user服务：初始化账户
        this.initAccount(regisInfo, userId);
//        int a = 1/0;
    }

    @Override
    public String addUser(RegisDTO regisInfo, String id) {
        long current = System.currentTimeMillis();
        User user = new User();
        user.setId(String.valueOf(id)); // 使用传入的id，确保与Login和Employee保持一致
        user.setPhone(regisInfo.getMobile());
        user.setCreateTime(current);
        user.setUpdateTime(current);
        user.setNickName("小可爱");
        user.setLoginId(id); // loginId字段也设置为相同的id
        save(user);
        return user.getId();
    }

    @Override
    public void initAccount(RegisDTO regisInfo,String userId) {
        UserAccount userAccount = new UserAccount();
        userAccount.setId(userId);
        userAccount.setFrozenAmount(0.0);
        userAccount.setUsableAmount(0.0);
        userAccount.setCreateTime(System.currentTimeMillis());
        userAccount.setUpdateTime(System.currentTimeMillis());

        accountService.save(userAccount);
    }
}
