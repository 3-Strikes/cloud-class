package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.domain.Permission;

import java.util.List;

/**
 * <p>
 * 权限表 Mapper 接口
 * </p>
 *
 * @author fyt
 * @since 2025-12-05
 */
public interface PermissionMapper extends BaseMapper<Permission> {

    List<Permission> selectPerms(Long loginId);
}
