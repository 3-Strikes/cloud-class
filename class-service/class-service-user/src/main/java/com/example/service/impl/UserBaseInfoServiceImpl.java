package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.domain.UserBaseInfo;
import com.example.mapper.UserBaseInfoMapper;
import com.example.service.UserBaseInfoService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 会员基本信息 服务实现类
 * </p>
 *
 * @author fyt
 * @since 2025-12-05
 */
@Service
public class UserBaseInfoServiceImpl extends ServiceImpl<UserBaseInfoMapper, UserBaseInfo> implements UserBaseInfoService {

}
