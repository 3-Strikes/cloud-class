package com.example.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.domain.*;
import com.example.dto.CourseDTO;
import com.example.mapper.*;
import com.example.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lzy
 * @since 2025-12-08
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
    @Autowired
    private TeacherService teacherService;

    @Autowired
    private CourseDetailService detailService;

    @Autowired
    private CourseMarketService marketService;
    @Autowired
    private CourseResourceService resourceService;

    @Autowired
    private CourseDetailMapper courseDetailMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CourseResourceMapper courseResourceMapper;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private CourseChapterMapper courseChapterMapper;
    //本地事务
    @Transactional
    @Override
    public void saveCourseDTO(CourseDTO courseDTO) {
        //根据讲师id查询讲师名称
        List<Long> teacharIds = courseDTO.getTeacharIds();
        List<String> names=teacherService.selectNamesByIds(teacharIds);
        //保存课程信息
        Course course = courseDTO.getCourse();
        course.setStatus(0);
        course.setLoginId(0L);
        course.setLoginUserName("test");
        course.setTeacherNames(CollectionUtil.join(names, ","));
        save(course);
        //保存课程详情信息
        CourseDetail courseDetail = courseDTO.getCourseDetail();
        courseDetail.setId(course.getId());
        detailService.save(courseDetail);
        //保存课程营销信息
        CourseMarket courseMarket = courseDTO.getCourseMarket();
        courseMarket.setId(course.getId());
        if(courseMarket.getPrice()==null)courseMarket.setPrice(new BigDecimal(0.0));
        if(courseMarket.getPriceOld()==null)courseMarket.setPriceOld(new BigDecimal(0.0));
        marketService.save(courseMarket);

        //保存课程资源信息
        CourseResource courseResource = courseDTO.getCourseResource();
        courseResource.setCourseId(course.getId());
        resourceService.save(courseResource);
    }

    @Override
    public CourseDTO getCourseDTOById(Long id) {
        CourseDTO dto = new CourseDTO();

        // 1. 课程基本信息
        Course course = this.getById(id);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }
        dto.setCourse(course);

        // 2. 课程详情
        CourseDetail detail = courseDetailMapper.selectOne(
                new LambdaQueryWrapper<CourseDetail>().eq(CourseDetail::getId, id));
        dto.setCourseDetail(detail);

        // 3. 课程营销信息
        CourseMarket market = courseMarketMapper.selectOne(
                new LambdaQueryWrapper<CourseMarket>().eq(CourseMarket::getId, id));
        dto.setCourseMarket(market);

        // 4. 课程资源
        CourseResource resource = courseResourceMapper.selectOne(
                new LambdaQueryWrapper<CourseResource>().eq(CourseResource::getCourseId, id));
        dto.setCourseResource(resource);

        // 5. 授课老师ID列表
        List<CourseTeacher> courseTeachers = teacherMapper.selectList(
                new LambdaQueryWrapper<CourseTeacher>().eq(CourseTeacher::getCourseId, id));
        dto.setTeacharIds(courseTeachers.stream().map(CourseTeacher::getTeacherId).collect(Collectors.toList()));

        return dto;
    }

    @Override
    public void updateCourseChapterCount(Long courseId) {
        // 1. 查询该课程的章节总数
        Integer chapterCount = courseChapterMapper.countByCourseId(courseId);

        // 2. 更新课程表的章节数字段
        Course course = new Course();
        course.setId(courseId);
        course.setChapterCount(chapterCount);
        this.updateById(course);
    }
}
