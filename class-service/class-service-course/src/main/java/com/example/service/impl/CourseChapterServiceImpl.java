// CourseChapterServiceImpl实现类
package com.example.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.domain.CourseChapter;
import com.example.mapper.CourseChapterMapper;
import com.example.service.CourseChapterService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseChapterServiceImpl extends ServiceImpl<CourseChapterMapper, CourseChapter> implements CourseChapterService {

    @Override
    public Integer getMaxChapterNumber(Long courseId) {
        return this.baseMapper.getMaxChapterNumber(courseId);
    }

    @Override
    public List<CourseChapter> listByCourseId(Long courseId) {
        return this.list(Wrappers.query(CourseChapter.class).eq("course_id", courseId));
    }
}