package com.example.web.controller;

import com.example.service.CourseViewLogService;
import com.example.domain.CourseViewLog;
import com.example.query.CourseViewLogQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courseViewLog")
public class CourseViewLogController {

    @Autowired
    public CourseViewLogService courseViewLogService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody CourseViewLog courseViewLog){
        if(courseViewLog.getId()!=null){
            courseViewLogService.updateById(courseViewLog);
        }else{
            courseViewLogService.save(courseViewLog);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        courseViewLogService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(courseViewLogService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(courseViewLogService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody CourseViewLogQuery query){
        Page<CourseViewLog> page = new Page<CourseViewLog>(query.getPage(),query.getRows());
        page = courseViewLogService.page(page);
        return JSONResult.success(new PageList<CourseViewLog>(page.getTotal(),page.getRecords()));
    }
}
