package com.example.service;

import com.example.domain.Course;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dto.CourseDTO;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lzy
 * @since 2025-12-08
 */
public interface CourseService extends IService<Course> {

    //本地事务
    @Transactional
    void saveCourseDTO(CourseDTO courseDTO);

    CourseDTO getCourseDTOById(Long id);

    void updateCourseChapterCount(Long courseId);
}
