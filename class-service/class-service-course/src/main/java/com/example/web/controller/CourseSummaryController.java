package com.example.web.controller;

import com.example.service.CourseSummaryService;
import com.example.domain.CourseSummary;
import com.example.query.CourseSummaryQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courseSummary")
public class CourseSummaryController {

    @Autowired
    public CourseSummaryService courseSummaryService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody CourseSummary courseSummary){
        if(courseSummary.getId()!=null){
            courseSummaryService.updateById(courseSummary);
        }else{
            courseSummaryService.save(courseSummary);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        courseSummaryService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(courseSummaryService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(courseSummaryService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody CourseSummaryQuery query){
        Page<CourseSummary> page = new Page<CourseSummary>(query.getPage(),query.getRows());
        page = courseSummaryService.page(page);
        return JSONResult.success(new PageList<CourseSummary>(page.getTotal(),page.getRecords()));
    }
}
