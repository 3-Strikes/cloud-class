package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.CourseOrderItem;
import com.example.query.CourseOrderItemQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.CourseOrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courseOrderItem")
public class CourseOrderItemController {

    @Autowired
    public CourseOrderItemService courseOrderItemService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody CourseOrderItem courseOrderItem){
        if(courseOrderItem.getId()!=null){
            courseOrderItemService.updateById(courseOrderItem);
        }else{
            courseOrderItemService.save(courseOrderItem);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        courseOrderItemService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(courseOrderItemService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(courseOrderItemService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody CourseOrderItemQuery query){
        Page<CourseOrderItem> page = new Page<CourseOrderItem>(query.getPage(),query.getRows());
        page = courseOrderItemService.page(page);
        return JSONResult.success(new PageList<CourseOrderItem>(page.getTotal(),page.getRecords()));
    }
}
