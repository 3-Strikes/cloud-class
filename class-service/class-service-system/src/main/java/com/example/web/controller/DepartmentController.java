package com.example.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.Department;
import com.example.query.DepartmentQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/department")
public class DepartmentController {

    @Autowired
    public DepartmentService departmentService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody Department department){
        if(department.getId()!=null){
            departmentService.updateById(department);
        }else{
            departmentService.save(department);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        departmentService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(departmentService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(departmentService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody DepartmentQuery query){
        Page<Department> page = new Page<Department>(query.getPage(),query.getRows());
        page = departmentService.page(page);
        return JSONResult.success(new PageList<Department>(page.getTotal(),page.getRecords()));
    }
}
