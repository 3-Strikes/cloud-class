package com.example.service;

import com.example.domain.CourseChapter;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 课程章节 ， 一个课程，多个章节，一个章节，多个视频 服务类
 * </p>
 *
 * @author lzy
 * @since 2025-12-08
 */
public interface CourseChapterService extends IService<CourseChapter> {

    Integer getMaxChapterNumber(Long courseId);
}
