package com.example.service.impl;

import com.example.domain.Teacher;
import com.example.mapper.TeacherMapper;
import com.example.service.TeacherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 老师表 服务实现类
 * </p>
 *
 * @author lzy
 * @since 2025-12-08
 */
@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    @Override
    public List<String> selectNamesByIds(List<Long> teacharIds) {
        //mybatis-plus
//        LambdaQueryWrapper<Teacher> w = Wrappers.lambdaQuery(Teacher.class).in(Teacher::getId, teacharIds).select(Teacher::getName);
//        List<Teacher> list = this.list(w);
//        list.stream().map(Teacher::getName).collect(Collectors.toList())
        //mybatis
        List<String> names=super.baseMapper.selectNamesByIds(teacharIds);
        return names;
    }

    @Override
    public List<Teacher> listByCourseId(Long courseId) {
        return super.baseMapper.listByCourseId(courseId);
    }
}
