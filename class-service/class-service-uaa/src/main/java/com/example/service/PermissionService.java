package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.Permission;

import java.util.List;

/**
 * <p>
 * 权限表 服务类
 * </p>
 *
 * @author fyt
 * @since 2025-12-05
 */
public interface PermissionService extends IService<Permission> {

    List<Permission> selectPerms(String id);
}
