package com.example.web.controller;

import com.example.service.TeacherService;
import com.example.domain.Teacher;
import com.example.query.TeacherQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    public TeacherService teacherService;

    /**
     * 保存和修改公用的
     */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody Teacher teacher){
        if(teacher.getId()!=null){
            teacherService.updateById(teacher);
        }else{
            teacherService.save(teacher);
        }
        return JSONResult.success();
    }

    /**
     * 删除对象
     */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        teacherService.removeById(id);
        return JSONResult.success();
    }

    /**
     * 获取对象
     */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(teacherService.getById(id));
    }


    /**
     * 查询所有对象
     */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(teacherService.list());
    }


    /**
     * 带条件分页查询数据
     */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody TeacherQuery query){
        Page<Teacher> page = new Page<Teacher>(query.getPage(),query.getRows());
        page = teacherService.page(page);
        return JSONResult.success(new PageList<Teacher>(page.getTotal(),page.getRecords()));
    }
}
