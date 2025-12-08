package com.example.web.controller;

import com.example.service.CourseUserLearnService;
import com.example.domain.CourseUserLearn;
import com.example.query.CourseUserLearnQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courseUserLearn")
public class CourseUserLearnController {

    @Autowired
    public CourseUserLearnService courseUserLearnService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody CourseUserLearn courseUserLearn){
        if(courseUserLearn.getId()!=null){
            courseUserLearnService.updateById(courseUserLearn);
        }else{
            courseUserLearnService.save(courseUserLearn);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        courseUserLearnService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(courseUserLearnService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(courseUserLearnService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody CourseUserLearnQuery query){
        Page<CourseUserLearn> page = new Page<CourseUserLearn>(query.getPage(),query.getRows());
        page = courseUserLearnService.page(page);
        return JSONResult.success(new PageList<CourseUserLearn>(page.getTotal(),page.getRecords()));
    }
}
