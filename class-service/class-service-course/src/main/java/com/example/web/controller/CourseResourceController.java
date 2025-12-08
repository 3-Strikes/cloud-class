package com.example.web.controller;

import com.example.service.CourseResourceService;
import com.example.domain.CourseResource;
import com.example.query.CourseResourceQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courseResource")
public class CourseResourceController {

    @Autowired
    public CourseResourceService courseResourceService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody CourseResource courseResource){
        if(courseResource.getId()!=null){
            courseResourceService.updateById(courseResource);
        }else{
            courseResourceService.save(courseResource);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        courseResourceService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(courseResourceService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(courseResourceService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody CourseResourceQuery query){
        Page<CourseResource> page = new Page<CourseResource>(query.getPage(),query.getRows());
        page = courseResourceService.page(page);
        return JSONResult.success(new PageList<CourseResource>(page.getTotal(),page.getRecords()));
    }
}
