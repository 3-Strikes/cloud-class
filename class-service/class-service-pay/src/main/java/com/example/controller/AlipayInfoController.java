package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.AlipayInfo;
import com.example.query.AlipayInfoQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.AlipayInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alipayInfo")
public class AlipayInfoController {

    @Autowired
    public AlipayInfoService alipayInfoService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody AlipayInfo alipayInfo){
        if(alipayInfo.getId()!=null){
            alipayInfoService.updateById(alipayInfo);
        }else{
            alipayInfoService.save(alipayInfo);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        alipayInfoService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(alipayInfoService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(alipayInfoService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody AlipayInfoQuery query){
        Page<AlipayInfo> page = new Page<AlipayInfo>(query.getPage(),query.getRows());
        page = alipayInfoService.page(page);
        return JSONResult.success(new PageList<AlipayInfo>(page.getTotal(),page.getRecords()));
    }
}
