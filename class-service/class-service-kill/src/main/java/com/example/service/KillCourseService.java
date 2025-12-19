package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.KillCourse;
import com.example.vo.CourseDetailVO;
import com.example.vo.KillCourseVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author fyt
 * @since 2025-12-17
 */
public interface KillCourseService extends IService<KillCourse> {

    List<KillCourse> listAllOnlineKillCourse();

    CourseDetailVO getCourseDetailFromCache(String actId, String courseId);

    KillCourseVO getOnlineKillCourse(String killId, String actId);
}
