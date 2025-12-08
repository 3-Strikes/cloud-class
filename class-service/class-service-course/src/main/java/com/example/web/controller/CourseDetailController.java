package com.example.web.controller;

import com.example.service.CourseDetailService;
import com.example.domain.CourseDetail;
import com.example.query.CourseDetailQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courseDetail")
public class CourseDetailController {

    @Autowired
    public CourseDetailService courseDetailService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody CourseDetail courseDetail){
        if(courseDetail.getId()!=null){
            courseDetailService.updateById(courseDetail);
        }else{
            courseDetailService.save(courseDetail);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        courseDetailService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(courseDetailService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(courseDetailService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody CourseDetailQuery query){
        Page<CourseDetail> page = new Page<CourseDetail>(query.getPage(),query.getRows());
        page = courseDetailService.page(page);
        return JSONResult.success(new PageList<CourseDetail>(page.getTotal(),page.getRecords()));
    }
}
