package com.example.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.Config;
import com.example.query.ConfigQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config")
public class ConfigController {

    @Autowired
    public ConfigService configService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody Config config){
        if(config.getId()!=null){
            configService.updateById(config);
        }else{
            configService.save(config);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        configService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(configService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(configService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody ConfigQuery query){
        Page<Config> page = new Page<Config>(query.getPage(),query.getRows());
        page = configService.page(page);
        return JSONResult.success(new PageList<Config>(page.getTotal(),page.getRecords()));
    }
}
