package com.example.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cache.CacheService;
import com.example.constant.CacheKeys;
import com.example.domain.KillActivity;
import com.example.domain.KillCourse;
import com.example.enums.PublishStatus;
import com.example.exceptions.BusinessException;
import com.example.mapper.KillActivityMapper;
import com.example.service.KillActivityService;
import com.example.service.KillCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
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
        //4.缓存课程信息
        Map<String,KillCourse> courseMap = killCourseList.stream().collect(Collectors.toMap(KillCourse::getId, killCourse -> killCourse));
        cacheService.hput(CacheKeys.KILL_ACTIVITY+actId,courseMap);
    }
}
