package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.UserBaseInfo;
import com.example.query.UserBaseInfoQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.UserBaseInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/userBaseInfo")
public class UserBaseInfoController {

    @Autowired
    public UserBaseInfoService userBaseInfoService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody UserBaseInfo userBaseInfo){
        if(userBaseInfo.getId()!=null){
            userBaseInfoService.updateById(userBaseInfo);
        }else{
            userBaseInfoService.save(userBaseInfo);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        userBaseInfoService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(userBaseInfoService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(userBaseInfoService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody UserBaseInfoQuery query){
        Page<UserBaseInfo> page = new Page<UserBaseInfo>(query.getPage(),query.getRows());
        page = userBaseInfoService.page(page);
        return JSONResult.success(new PageList<UserBaseInfo>(page.getTotal(),page.getRecords()));
    }
}
