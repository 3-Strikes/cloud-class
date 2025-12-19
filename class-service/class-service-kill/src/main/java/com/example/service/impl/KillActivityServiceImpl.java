package com.example.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.CourseServiceAPI;
import com.example.cache.CacheService;
import com.example.constant.CacheKeys;
import com.example.domain.KillActivity;
import com.example.domain.KillCourse;
import com.example.enums.PublishStatus;
import com.example.exceptions.BusinessException;
import com.example.mapper.KillActivityMapper;
import com.example.result.JSONResult;
import com.example.service.KillActivityService;
import com.example.service.KillCourseService;
import com.example.vo.CourseDetailVO;
import com.example.vo.KillCourseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author fyt
 * @since 2025-12-17
 */
@Service
public class KillActivityServiceImpl extends ServiceImpl<KillActivityMapper, KillActivity> implements KillActivityService {

    @Autowired
    private CacheService cacheService;
    @Autowired
    private KillCourseService killCourseService;

    @Autowired
    private CourseServiceAPI courseServiceAPI;
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void publish(Long actId) {
        //1.先查询秒杀活动
        KillActivity act = this.getById(actId);
        if(act==null)throw new BusinessException("活动不存在");
        if(act.getPublishStatus()==PublishStatus.PUBLISH_SUC.getCode())throw new BusinessException("活动已发布");

        Date date = new Date();

        //2.修改活动发布状态，发布时间
        act.setPublishStatus(PublishStatus.PUBLISH_SUC.getCode());
        act.setPublishTime(date);
        this.updateById(act);
        //4.修改课程发布状态，发布时间
        killCourseService.update(Wrappers.lambdaUpdate(KillCourse.class)
                .set(KillCourse::getPublishStatus, PublishStatus.PUBLISH_SUC.getCode())
                .set(KillCourse::getPublishTime, date)
                .eq(KillCourse::getActivityId, actId));

        //3.查询活动下课程信息
        List<KillCourse> killCourseList = killCourseService.list(Wrappers.lambdaQuery(KillCourse.class).eq(KillCourse::getActivityId, actId));
        //4.缓存课程信息\
        Map<String,KillCourse> courseMap = new HashMap<>();
        for (KillCourse killCourse : killCourseList) {
            courseMap.put(killCourse.getCourseId().toString(),killCourse);
        }
        cacheService.hput(CacheKeys.KILL_ACTIVITY+actId,courseMap);
        //5.缓存秒杀课程的详细信息（价格，章节，视频，详情）
        List<Long> courseIds = killCourseList.stream().map(killCourse -> killCourse.getCourseId()).collect(Collectors.toList());
        JSONResult<List<CourseDetailVO>> listJSONResult = courseServiceAPI.courseDetailData(courseIds);
        List<CourseDetailVO> detailList = listJSONResult.getData();
        //把课程的详细信息缓存redis，存成hash类型，大key：ymcc:kill:activity:detail:1。  key为课程id，value为课程的详细信息
        Map<String,CourseDetailVO> courseDetailMap=new HashMap<>();
        for (CourseDetailVO courseDetailVO : detailList) {
            courseDetailMap.put(courseDetailVO.getCourse().getId().toString(), courseDetailVO);
        }
        cacheService.hput(CacheKeys.KILL_ACTIVITY_COURSE_DETAIL+actId,courseDetailMap);

        //6.秒杀课程的库存量信息
        for (KillCourse killCourse : killCourseList) {
            cacheService.set(CacheKeys.KILL_ACTIVITY_COURSE_COUNT+actId+":"+killCourse.getCourseId(),killCourse.getKillCount());
        }

        //是否可以有多个秒杀活动同时发布状态？？？？
        //redis中有多个hash集合，要从多个hash集合中获取KillCourse集合

    }

}
