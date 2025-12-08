package com.example.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.domain.CourseTeacher;
import com.example.domain.Teacher;

import java.util.List;

/**
 * <p>
 * 老师表 Mapper 接口
 * </p>
 *
 * @author lzy
 * @since 2025-12-08
 */
public interface TeacherMapper extends BaseMapper<Teacher> {

    List<String> selectNamesByIds(List<Long> teacharIds);

    List<CourseTeacher> selectList(LambdaQueryWrapper<CourseTeacher> eq);
}
