package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.domain.CourseChapter;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 课程章节 ， 一个课程，多个章节，一个章节，多个视频 Mapper 接口
 * </p>
 *
 * @author lzy
 * @since 2025-12-08
 */
public interface CourseChapterMapper extends BaseMapper<CourseChapter> {

    /**
     * 根据课程ID统计章节数
     */
    @Select("SELECT COUNT(*) FROM t_course_chapter WHERE course_id = #{courseId}")
    Integer countByCourseId(@Param("courseId") Long courseId);

    /**
     * 查询课程下最大的章节号
     */
    @Select("SELECT IFNULL(MAX(number),0) FROM t_course_chapter WHERE course_id = #{courseId}")
    Integer getMaxChapterNumber(@Param("courseId") Long courseId);
}
