package com.example.service;

import com.example.doc.CourseDoc;
import com.example.domain.Course;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dto.CourseDTO;
import com.example.vo.CourseDetailVO;
import com.example.vo.CourseOrderVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

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

    List<CourseDoc> listCourseDoc(Set<Long> ids);

    void sendPuslishMessage(List<CourseDoc> courseDocList);

    CourseOrderVO getCourseInfoByIds(List<Long> courseIds);

    CourseDetailVO getDetailById(Long courseId);
}
