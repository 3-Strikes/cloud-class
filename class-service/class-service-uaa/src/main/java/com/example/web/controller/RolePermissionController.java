package com.example.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.RolePermission;
import com.example.query.RolePermissionQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rolePermission")
public class RolePermissionController {

    @Autowired
    public RolePermissionService rolePermissionService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody RolePermission rolePermission){
        if(rolePermission.getId()!=null){
            rolePermissionService.updateById(rolePermission);
        }else{
            rolePermissionService.save(rolePermission);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        rolePermissionService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(rolePermissionService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(rolePermissionService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody RolePermissionQuery query){
        Page<RolePermission> page = new Page<RolePermission>(query.getPage(),query.getRows());
        page = rolePermissionService.page(page);
        return JSONResult.success(new PageList<RolePermission>(page.getTotal(),page.getRecords()));
    }
}
