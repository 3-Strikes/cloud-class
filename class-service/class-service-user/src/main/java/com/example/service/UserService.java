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

    Long addUser(RegisDTO regisInfo,Long loginId);

    void initAccount(RegisDTO regisInfo,Long userId);

    Long addUaaLogin(RegisDTO regisInfo);

    void regis(RegisDTO regisInfo);

}
