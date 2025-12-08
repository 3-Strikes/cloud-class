package com.example.web.controller;

import com.example.service.CourseTeacherService;
import com.example.domain.CourseTeacher;
import com.example.query.CourseTeacherQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courseTeacher")
public class CourseTeacherController {

    @Autowired
    public CourseTeacherService courseTeacherService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody CourseTeacher courseTeacher){
        if(courseTeacher.getId()!=null){
            courseTeacherService.updateById(courseTeacher);
        }else{
            courseTeacherService.save(courseTeacher);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        courseTeacherService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(courseTeacherService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(courseTeacherService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody CourseTeacherQuery query){
        Page<CourseTeacher> page = new Page<CourseTeacher>(query.getPage(),query.getRows());
        page = courseTeacherService.page(page);
        return JSONResult.success(new PageList<CourseTeacher>(page.getTotal(),page.getRecords()));
    }
}
