package com.example.web.controller;

import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.Systemdictionary;
import com.example.query.SystemdictionaryQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.SystemdictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/systemdictionary")
public class SystemdictionaryController {

    @Autowired
    public SystemdictionaryService systemdictionaryService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody Systemdictionary systemdictionary){
        if(systemdictionary.getId()!=null){
            systemdictionaryService.updateById(systemdictionary);
        }else{
            systemdictionaryService.save(systemdictionary);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        systemdictionaryService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(systemdictionaryService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(systemdictionaryService.list());
    }


    /**
     * 带条件分页查询数据（补充关键字查询逻辑）
     */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody SystemdictionaryQuery query){
        // 1. 构建分页对象
        Page<Systemdictionary> page = new Page<Systemdictionary>(query.getPage(),query.getRows());

        // 2. 构建查询条件：name字段模糊匹配
        LambdaQueryWrapper<Systemdictionary> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getName())) { // 关键字非空时才添加条件
            wrapper.like(Systemdictionary::getName, query.getName());
        }

        // 3. 执行带条件的分页查询（替换原有的无条件page调用）
        page = systemdictionaryService.page(page, wrapper);

        return JSONResult.success(new PageList<Systemdictionary>(page.getTotal(),page.getRecords()));
    }
}
