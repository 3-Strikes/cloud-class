package com.example.service;

import com.example.domain.Teacher;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 老师表 服务类
 * </p>
 *
 * @author lzy
 * @since 2025-12-08
 */
public interface TeacherService extends IService<Teacher> {

    List<String> selectNamesByIds(List<Long> teacharIds);

    List<Teacher> listByCourseId(Long courseId);
}
