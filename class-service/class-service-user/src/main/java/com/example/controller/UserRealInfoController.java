package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.UserRealInfo;
import com.example.query.UserRealInfoQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.UserRealInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/userRealInfo")
public class UserRealInfoController {

    @Autowired
    public UserRealInfoService userRealInfoService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody UserRealInfo userRealInfo){
        if(userRealInfo.getId()!=null){
            userRealInfoService.updateById(userRealInfo);
        }else{
            userRealInfoService.save(userRealInfo);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        userRealInfoService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(userRealInfoService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(userRealInfoService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody UserRealInfoQuery query){
        Page<UserRealInfo> page = new Page<UserRealInfo>(query.getPage(),query.getRows());
        page = userRealInfoService.page(page);
        return JSONResult.success(new PageList<UserRealInfo>(page.getTotal(),page.getRecords()));
    }
}
