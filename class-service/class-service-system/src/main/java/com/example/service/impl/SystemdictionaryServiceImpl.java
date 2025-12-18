package com.example.service.impl;

import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.domain.Systemdictionary;
import com.example.mapper.SystemdictionaryMapper;
import com.example.query.SystemdictionaryQuery;
import com.example.service.SystemdictionaryService;
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
public class SystemdictionaryServiceImpl extends ServiceImpl<SystemdictionaryMapper, Systemdictionary> implements SystemdictionaryService {

    @Override
    public Page<Systemdictionary> pageList(SystemdictionaryQuery query) {
        // 1. 构建分页对象
        Page<Systemdictionary> page = new Page<>(query.getPage(), query.getRows());

        // 2. 构建查询条件（name模糊匹配）
        LambdaQueryWrapper<Systemdictionary> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getName())) {
            wrapper.like(Systemdictionary::getName, query.getName());
        }
        // 按ID降序排序
        wrapper.orderByDesc(Systemdictionary::getId);

        // 3. 执行分页查询
        return this.page(page, wrapper);
    }
}
