package com.example.web.controller;

import com.alibaba.nacos.common.utils.StringUtils;
import com.example.domain.CourseType;
import com.example.service.CourseService;
import com.example.domain.Course;
import com.example.query.CourseQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.service.CourseTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    public CourseService courseService;

    @Autowired
    public CourseTypeService courseTypeService;

    @GetMapping("treeData")
    public JSONResult treeData(){
        List<CourseType> tree=courseTypeService.buildTreeData();
        return JSONResult.success(tree);
    }
    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody Course course){
        if(course.getId()!=null){
            courseService.updateById(course);
        }else{
            courseService.save(course);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        courseService.removeById(id);
        return JSONResult.success();
    }

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

    /**
     * 批量删除课程
     */
    @RequestMapping(value="/batch/{ids}",method=RequestMethod.DELETE)
    public JSONResult batchDelete(@PathVariable("ids") String ids){
        if(!StringUtils.hasText(ids)){
            return JSONResult.error("删除ID不能为空");
        }
        try {
            List<Long> idList = Arrays.stream(ids.split(","))
                    .map(Long::parseLong)
                    .toList();
            courseService.removeByIds(idList);
            return JSONResult.success("批量删除成功");
        } catch (Exception e) {
            return JSONResult.error("批量删除异常：" + e.getMessage());
        }
    }
}
