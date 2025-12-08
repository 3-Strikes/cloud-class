package com.example.service.impl;

import com.example.domain.CourseChapter;
import com.example.mapper.CourseChapterMapper;
import com.example.service.CourseChapterService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 课程章节 ， 一个课程，多个章节，一个章节，多个视频 服务实现类
 * </p>
 *
 * @author lzy
 * @since 2025-12-08
 */
@Service
public class CourseChapterServiceImpl extends ServiceImpl<CourseChapterMapper, CourseChapter> implements CourseChapterService {

    @Override
    public Integer getMaxChapterNumber(Long courseId) {
        return this.baseMapper.getMaxChapterNumber(courseId);
    }
}
