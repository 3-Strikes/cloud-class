package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // 必须导入MyBatis-Plus的BaseMapper
import com.example.domain.CourseChapter;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

// 关键：继承 BaseMapper<CourseChapter>，泛型必须是实体类CourseChapter
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