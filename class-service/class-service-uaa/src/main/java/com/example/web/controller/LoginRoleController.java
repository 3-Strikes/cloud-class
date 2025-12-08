package com.example.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.LoginRole;
import com.example.query.LoginRoleQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.LoginRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loginRole")
public class LoginRoleController {

    @Autowired
    public LoginRoleService loginRoleService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody LoginRole loginRole){
        if(loginRole.getId()!=null){
            loginRoleService.updateById(loginRole);
        }else{
            loginRoleService.save(loginRole);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        loginRoleService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(loginRoleService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(loginRoleService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody LoginRoleQuery query){
        Page<LoginRole> page = new Page<LoginRole>(query.getPage(),query.getRows());
        page = loginRoleService.page(page);
        return JSONResult.success(new PageList<LoginRole>(page.getTotal(),page.getRecords()));
    }
}
