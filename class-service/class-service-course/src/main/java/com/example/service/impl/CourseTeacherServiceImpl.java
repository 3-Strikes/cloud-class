package com.example.service.impl;

import com.example.domain.CourseTeacher;
import com.example.mapper.CourseTeacherMapper;
import com.example.service.CourseTeacherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 课程和老师的中间表 服务实现类
 * </p>
 *
 * @author lzy
 * @since 2025-12-08
 */
@Service
public class CourseTeacherServiceImpl extends ServiceImpl<CourseTeacherMapper, CourseTeacher> implements CourseTeacherService {

}
