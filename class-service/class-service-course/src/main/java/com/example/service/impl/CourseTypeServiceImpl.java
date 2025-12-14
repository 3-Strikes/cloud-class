package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.cache.CacheService;
import com.example.constant.CacheKeys;
import com.example.domain.CourseType;
import com.example.mapper.CourseTypeMapper;
import com.example.service.CourseTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vo.CrumbsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.sql.Wrapper;
import java.util.*;

/**
 * <p>
 * 课程目录 服务实现类
 * </p>
 *
 * @author lzy
 * @since 2025-12-08
 */
@Service
public class CourseTypeServiceImpl extends ServiceImpl<CourseTypeMapper, CourseType> implements CourseTypeService {

    @Autowired
    private CacheService cacheService;
    @CacheEvict(cacheNames = CacheKeys.COURSE_TYPE_TREE,key = "1111")
    @Override
    public boolean save(CourseType entity) {
        boolean save = super.save(entity);
        return save;
    }

    @CacheEvict(cacheNames = CacheKeys.COURSE_TYPE_TREE,key = "1111")
    @Override
    public boolean updateById(CourseType entity) {
        boolean b = super.updateById(entity);
        return b;
    }

    //先走方法，方法结束，删缓存
    @CacheEvict(cacheNames ="ymcc:service-course:courseType",allEntries = true)
    @Override
    public boolean removeById(Serializable id) {
        boolean b = super.removeById(id);
        return b;
    }

    @Cacheable(cacheNames = "ymcc:service-course:courseType",key = "#id")
    @Override
    public CourseType getById(Serializable id) {
        return super.getById(id);
    }



    //此方法启用缓存，String key= CacheKeys.COURSE_TYPE_TREE+"::"+1111
    //先查缓存，有则返回，无则执行方法，把方法返回结果保存数据库。
    @Cacheable(cacheNames = CacheKeys.COURSE_TYPE_TREE, key = "1111")
    @Override
    public List<CourseType> buildTreeData(){
        List<CourseType> list = this.list();

        Map<Long, CourseType> map = new HashMap<>();
        for (CourseType courseType : list) {
            map.put(courseType.getId(), courseType);
        }
        List<CourseType> tree = new ArrayList<>();
        long root = 0;
        for (CourseType courseType : list) {
            Long pid = courseType.getPid();
            if (pid == root) {
                tree.add(courseType);
            } else {
                CourseType parent = map.get(pid);
                List<CourseType> children = parent.getChildren();
                if(children==null){
                    children = new ArrayList<>();
                    parent.setChildren(children);
                }
                children.add(courseType);
            }
        }
        return tree;
    }

    @Override
    public List<CrumbsVO> getCrumbs(Long courseTypeId) {
        List<CrumbsVO> result = new ArrayList<>();
        CourseType type = getById(courseTypeId);
        String path = type.getPath();
        String[] typeIds = path.split("\\.");
        List<CourseType> list = this.listByIds(Arrays.asList(typeIds));

        for (CourseType courseType : list) {
            CrumbsVO vo = new CrumbsVO();
            vo.setOwnerProductType(courseType);

            Long pid = courseType.getPid();
            LambdaQueryWrapper<CourseType> eq = Wrappers.lambdaQuery(CourseType.class).eq(CourseType::getPid, pid).ne(CourseType::getId, courseType.getId());
            List<CourseType> courseTypes = this.list(eq);
            vo.setOtherProductTypes(courseTypes);
            result.add(vo);
        }
        return result;
    }
}
