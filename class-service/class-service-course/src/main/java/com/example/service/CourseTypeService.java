package com.example.service;

import com.example.domain.CourseType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 课程目录 服务类
 * </p>
 *
 * @author lzy
 * @since 2025-12-08
 */
public interface CourseTypeService extends IService<CourseType> {

    List<CourseType> buildTreeData();
}
