package com.example.service.impl;

import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.domain.Systemdictionary;
import com.example.domain.Systemdictionaryitem;
import com.example.mapper.SystemdictionaryitemMapper;
import com.example.query.SystemdictionaryQuery;
import com.example.query.SystemdictionaryitemQuery;
import com.example.service.SystemdictionaryitemService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lzy
 * @since 2025-12-04
 */
@Service
public class SystemdictionaryitemServiceImpl extends ServiceImpl<SystemdictionaryitemMapper, Systemdictionaryitem> implements SystemdictionaryitemService {


    @Override
    public Page<Systemdictionaryitem> pageList(SystemdictionaryitemQuery query) {
        // 1. 构建分页对象
        Page<Systemdictionaryitem> page = new Page<>(query.getPage(), query.getRows());

        // 2. 构建查询条件（name模糊匹配）
        LambdaQueryWrapper<Systemdictionaryitem> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getName())) {
            wrapper.like(Systemdictionaryitem::getName, query.getName());
        }
        // 按ID降序排序
        wrapper.orderByDesc(Systemdictionaryitem::getId);

        // 3. 执行分页查询
        return this.page(page, wrapper);
    }
}
