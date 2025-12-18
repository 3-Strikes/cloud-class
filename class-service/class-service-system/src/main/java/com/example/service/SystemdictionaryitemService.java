package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.Systemdictionaryitem;
import com.example.query.SystemdictionaryitemQuery;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lzy
 * @since 2025-12-04
 */
public interface SystemdictionaryitemService extends IService<Systemdictionaryitem> {
    Page<Systemdictionaryitem> pageList(SystemdictionaryitemQuery query);
}
