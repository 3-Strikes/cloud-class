package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.LoginLog;
import com.example.query.LoginLogQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.LoginLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loginLog")
public class LoginLogController {

    @Autowired
    public LoginLogService loginLogService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody LoginLog loginLog){
        if(loginLog.getId()!=null){
            loginLogService.updateById(loginLog);
        }else{
            loginLogService.save(loginLog);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        loginLogService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(loginLogService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(loginLogService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody LoginLogQuery query){
        Page<LoginLog> page = new Page<LoginLog>(query.getPage(),query.getRows());
        page = loginLogService.page(page);
        return JSONResult.success(new PageList<LoginLog>(page.getTotal(),page.getRecords()));
    }
}
