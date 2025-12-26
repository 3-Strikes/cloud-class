package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.User;
import com.example.dto.RegisDTO;

/**
 * <p>
 * 会员登录账号 服务类
 * </p>
 *
 * @author fyt
 * @since 2025-12-05
 */
public interface UserService extends IService<User> {

    String addUser(RegisDTO regisInfo, String id);

    void initAccount(RegisDTO regisInfo,String userId);

    String addUaaLogin(RegisDTO regisInfo, String id);

    void regis(RegisDTO regisInfo);

}
