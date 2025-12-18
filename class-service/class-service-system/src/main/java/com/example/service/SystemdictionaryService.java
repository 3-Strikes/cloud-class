package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.Systemdictionary;
import com.example.query.SystemdictionaryQuery;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lzy
 * @since 2025-12-04
 */
public interface SystemdictionaryService extends IService<Systemdictionary> {
    // 带条件分页查询
    Page<Systemdictionary> pageList(SystemdictionaryQuery query);
}
