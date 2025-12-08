package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.UserGrowLog;
import com.example.query.UserGrowLogQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.UserGrowLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/userGrowLog")
public class UserGrowLogController {

    @Autowired
    public UserGrowLogService userGrowLogService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody UserGrowLog userGrowLog){
        if(userGrowLog.getId()!=null){
            userGrowLogService.updateById(userGrowLog);
        }else{
            userGrowLogService.save(userGrowLog);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        userGrowLogService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(userGrowLogService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(userGrowLogService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody UserGrowLogQuery query){
        Page<UserGrowLog> page = new Page<UserGrowLog>(query.getPage(),query.getRows());
        page = userGrowLogService.page(page);
        return JSONResult.success(new PageList<UserGrowLog>(page.getTotal(),page.getRecords()));
    }
}
