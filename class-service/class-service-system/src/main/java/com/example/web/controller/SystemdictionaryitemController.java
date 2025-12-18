package com.example.web.controller;

import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.Systemdictionaryitem;
import com.example.query.SystemdictionaryitemQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.SystemdictionaryitemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/systemdictionaryitem")
public class SystemdictionaryitemController {

    @Autowired
    public SystemdictionaryitemService systemdictionaryitemService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody Systemdictionaryitem systemdictionaryitem){
        if(systemdictionaryitem.getId()!=null){
            systemdictionaryitemService.updateById(systemdictionaryitem);
        }else{
            systemdictionaryitemService.save(systemdictionaryitem);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        systemdictionaryitemService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(systemdictionaryitemService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(systemdictionaryitemService.list());
    }


    // 在SystemdictionaryitemController的pagelist方法中补充条件
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody SystemdictionaryitemQuery query){
        Page<Systemdictionaryitem> page = new Page<>(query.getPage(),query.getRows());
        // 补充name模糊查询条件
        LambdaQueryWrapper<Systemdictionaryitem> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getName())) {
            wrapper.like(Systemdictionaryitem::getName, query.getName());
        }
        page = systemdictionaryitemService.page(page, wrapper); // 带条件分页
        return JSONResult.success(new PageList<>(page.getTotal(),page.getRecords()));
    }
}
