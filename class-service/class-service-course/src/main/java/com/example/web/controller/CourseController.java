package com.example.web.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.api.MediaServiceAPI;
import com.example.api.SearchServiceAPI;
import com.example.doc.CourseDoc;
import com.example.domain.CourseType;
import com.example.dto.CourseDTO;
import com.example.enums.CourseStatus;
import com.example.service.CourseService;
import com.example.domain.Course;
import com.example.query.CourseQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.service.CourseTypeService;
import com.example.util.OssUtil;
import jakarta.validation.Valid;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    public CourseService courseService;

    @Autowired
    public CourseTypeService courseTypeService;

    @Autowired
    private MediaServiceAPI mediaServiceAPI;

    @Autowired
    private SearchServiceAPI searchServiceAPI;

    @Transactional
    @PostMapping("onLineCourse")
    public JSONResult onLineCourse(@RequestBody List<Long> courseIds){
        //检测课程状态(未发布，已推流)
        //先筛选出去已发布，只保理未发布的课程  id in (ids)  and status = 0
        List<Course> courseList = courseService.list(Wrappers.lambdaQuery(Course.class).in(Course::getId,courseIds).eq(Course::getStatus, CourseStatus.UN_LINE.getCode()));
        courseIds= courseList.stream().map(Course::getId).collect(Collectors.toList());//所有未发布的课程id
        if(courseIds.size()==0) return JSONResult.success();
        //课程视频状态检测(远程接口调用)
        JSONResult<Set<Long>> setJSONResult = mediaServiceAPI.listPushEnd(courseIds);
        Set<Long> ids = setJSONResult.getData();//所有视频已推流的课程id

        //2.修改课程状态（已发布） set status=已发布 ，onlineTime=new date（）  where id in ids
        courseService.update(Wrappers.lambdaUpdate(Course.class).set(Course::getStatus, CourseStatus.ON_LINE.getCode()).set(Course::getOnlineTime,new Date()).in(Course::getId,ids));

        //3.发布课程数据到es索引库,为了前端课程站点能够展示并搜索到课程数据.向ES中存数据，到底存什么哪些属性？
        // es中的文档对象  (课程名，课程分类，讲师，免费收费，现价，原价,时间，forUser，等级，销量数，评论数，浏览量，上架时间)
        List<CourseDoc> courseDocList =courseService.listCourseDoc(ids);
        searchServiceAPI.publishCourse2Es(courseDocList);

        //4.发送推广信息(站内信，短信，邮箱)。异步发送（mq（微服务），spring异步任务（单体项目））
        courseService.sendPuslishMessage(courseDocList);

        return JSONResult.success();
    }

    @GetMapping("/treeData")
    public JSONResult treeData(){
        List<CourseType> tree=courseTypeService.buildTreeData();
        return JSONResult.success(tree);
    }
    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult save(@RequestBody @Valid CourseDTO courseDTO){
        courseService.saveCourseDTO(courseDTO);
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
//    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
//    public JSONResult delete(@PathVariable("id") Long id){
//        courseService.removeById(id);
//        return JSONResult.success();
//    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(courseService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(courseService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody CourseQuery query){
        Page<Course> page = new Page<Course>(query.getPage(),query.getRows());
        page = courseService.page(page);
        return JSONResult.success(new PageList<Course>(page.getTotal(),page.getRecords()));
    }

    @GetMapping("/dto/{id}")
    public JSONResult getCourseDTO(@PathVariable("id") Long id){
        CourseDTO courseDTO = courseService.getCourseDTOById(id);
        return JSONResult.success(courseDTO);
    }

    /**
     * 删除课程（同步删除OSS文件）
     */
//    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
//    public JSONResult delete(@PathVariable("id") Long id){
//        try {
//            // 1. 查询课程信息，获取关联的OSS文件URL
//            Course course = courseService.getById(id);
//            if (course != null) {
//                // 1.1 删除封面文件（直接调用OssUtil，无需HTTP调用）
//                String picUrl = course.getPic();
//                if (StringUtils.isNotBlank(picUrl)) {
//                    try {
//                        OssUtil.del(OssUtil.subObjectName(picUrl));
//                    } catch (Exception e) {
//                        // 仅打印日志，不影响课程删除
//                        System.err.println("删除课程封面失败：" + e.getMessage());
//                    }
//                }
//
//                // 1.2 如果有课件字段，同理删除
//                // String resourceUrl = course.getResourceUrl();
//                // if (StringUtils.isNotBlank(resourceUrl)) {
//                //     OssUtil.del(OssUtil.subObjectName(resourceUrl));
//                // }
//            }
//
//            // 2. 删除课程数据
//            courseService.removeById(id);
//            return JSONResult.success("课程删除成功");
//        } catch (Exception e) {
//            return JSONResult.error("课程删除失败：" + e.getMessage());
//        }
//    }
//
//    /**
//     * 批量删除课程
//     */
//    @RequestMapping(value="/batch/{ids}",method=RequestMethod.DELETE)
//    public JSONResult batchDelete(@PathVariable("ids") String ids){
//        if(!org.springframework.util.StringUtils.hasText(ids)){
//            return JSONResult.error("删除ID不能为空");
//        }
//        try {
//            List<Long> idList = Arrays.stream(ids.split(","))
//                    .map(Long::parseLong)
//                    .toList();
//
//            // 批量删除前，先删除关联的OSS文件
//            List<Course> courses = courseService.listByIds(idList);
//            for (Course course : courses) {
//                if (course != null && StringUtils.isNotBlank(course.getPic())) {
//                    try {
//                        OssUtil.del(OssUtil.subObjectName(course.getPic()));
//                    } catch (Exception e) {
//                        System.err.println("批量删除课程封面失败：" + e.getMessage());
//                    }
//                }
//            }
//
//            // 删除课程数据
//            courseService.removeByIds(idList);
//            return JSONResult.success("批量删除成功");
//        } catch (Exception e) {
//            return JSONResult.error("批量删除异常：" + e.getMessage());
//        }
//    }
    /**
     * 获取课程完整DTO信息（用于编辑）
     */

}
