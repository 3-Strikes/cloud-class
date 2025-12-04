package com.example.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.Employee;
import com.example.query.EmployeeQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    public EmployeeService employeeService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody Employee employee){
        if(employee.getId()!=null){
            employeeService.updateById(employee);
        }else{
            employeeService.save(employee);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        employeeService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(employeeService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(employeeService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody EmployeeQuery query){
        Page<Employee> page = new Page<Employee>(query.getPage(),query.getRows());
        page = employeeService.page(page);
        return JSONResult.success(new PageList<Employee>(page.getTotal(),page.getRecords()));
    }
}
