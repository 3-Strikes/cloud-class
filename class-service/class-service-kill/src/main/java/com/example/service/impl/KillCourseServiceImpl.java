package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cache.CacheService;
import com.example.constant.CacheKeys;
import com.example.domain.KillCourse;
import com.example.exceptions.BusinessException;
import com.example.mapper.KillCourseMapper;
import com.example.service.KillCourseService;
import com.example.vo.CourseDetailVO;
import com.example.vo.KillCourseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author fyt
 * @since 2025-12-17
 */
@Service
public class KillCourseServiceImpl extends ServiceImpl<KillCourseMapper, KillCourse> implements KillCourseService {
    @Autowired
    private CacheService cacheService;

    @Override
    public List<KillCourse> listAllOnlineKillCourse() {
        List<KillCourse> result=new ArrayList<>();
        //从redis查询所有秒杀课程列表
        //1.调用redis的keys命令，查询指定的键的集合
        Set<String> keys = cacheService.keys(CacheKeys.KILL_ACTIVITY + "*");
        //2.以上查询所有已发布活动的key的集合后，进行遍历获取hash下的value 的集合
        for (String key : keys) {
            List killCourseList = cacheService.hvalues(key);
            result.addAll(killCourseList);
        }
        return result;
    }

    @Override
    public CourseDetailVO getCourseDetailFromCache(String actId, String courseId) {
        Object hget = cacheService.hget(CacheKeys.KILL_ACTIVITY_COURSE_DETAIL + actId, courseId);
        if (hget != null) {
            return (CourseDetailVO) hget;
        }
        return new CourseDetailVO();
    }

    @Override
    public KillCourseVO getOnlineKillCourse(String killId, String actId) {
        Object hget = cacheService.hget(CacheKeys.KILL_ACTIVITY + actId, killId);
        if(hget==null)throw new BusinessException("秒杀课程不存在 ");
        Date now=new Date();
        KillCourse killCourse = (KillCourse) hget;
        KillCourseVO result =new KillCourseVO();
        result.setKillPrice(killCourse.getKillPrice());
        result.setKilling(killCourse.getKilling());
        result.setUnbegin(now.before(killCourse.getStartTime()));
        if(result.getKilling()){
            result.setTimeDiffMill((killCourse.getEndTime().getTime()-now.getTime())/1000);
        }else if(result.getUnbegin()){
            result.setTimeDiffMill((killCourse.getStartTime().getTime()-now.getTime())/1000);
        }
        return result;
    }
}
