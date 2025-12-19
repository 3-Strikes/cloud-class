package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.KillActivity;
import com.example.domain.KillCourse;
import com.example.enums.PublishStatus;
import com.example.query.KillCourseQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.KillActivityService;
import com.example.service.KillCourseService;
import com.example.vo.CourseDetailVO;
import com.example.vo.KillCourseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/killCourse")
public class KillCourseController {

    @Autowired
    public KillCourseService killCourseService;

    @Autowired
    private KillActivityService killActivityService;

    @GetMapping("online/one/{killId}/{actId}")
    public JSONResult onlineKillCourse(@PathVariable String killId,@PathVariable String actId){
        KillCourseVO killCoursevo = killCourseService.getOnlineKillCourse(killId,actId);
        return JSONResult.success(killCoursevo);
    }

    @GetMapping("course/detail/{actId}/{courseId}")
    public JSONResult killCourseDetail(@PathVariable String actId,@PathVariable String courseId){
        CourseDetailVO detail= killCourseService.getCourseDetailFromCache(actId,courseId);
        return JSONResult.success(detail);
    }


    @GetMapping("online/all")
    public JSONResult killCourseList(){
        List<KillCourse> list= killCourseService.listAllOnlineKillCourse();
        return JSONResult.success(list);//对象转json字符串，底层是调用get方法获取每个属性值，作为json的值{“”：“”，}
    }

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody KillCourse killCourse){
        Date date = new Date();
        if(killCourse.getId()!=null){
            killCourseService.updateById(killCourse);
        }else{
            KillActivity byId = killActivityService.getById(killCourse.getActivityId());

            killCourse.setKillLimit(1);
            killCourse.setPublishStatus(PublishStatus.TO_PUBLISH.getCode());
            killCourse.setCreateTime(date);
            killCourse.setStartTime(byId.getBeginTime());
            killCourse.setEndTime(byId.getEndTime());
            killCourseService.save(killCourse);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        killCourseService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(killCourseService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(killCourseService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody KillCourseQuery query){
        Page<KillCourse> page = new Page<KillCourse>(query.getPage(),query.getRows());
        page = killCourseService.page(page);
        return JSONResult.success(new PageList<KillCourse>(page.getTotal(),page.getRecords()));
    }
}
