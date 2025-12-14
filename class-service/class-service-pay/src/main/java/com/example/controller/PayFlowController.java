package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.PayFlow;
import com.example.query.PayFlowQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.PayFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payFlow")
public class PayFlowController {

    @Autowired
    public PayFlowService payFlowService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody PayFlow payFlow){
        if(payFlow.getId()!=null){
            payFlowService.updateById(payFlow);
        }else{
            payFlowService.save(payFlow);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        payFlowService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(payFlowService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(payFlowService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody PayFlowQuery query){
        Page<PayFlow> page = new Page<PayFlow>(query.getPage(),query.getRows());
        page = payFlowService.page(page);
        return JSONResult.success(new PageList<PayFlow>(page.getTotal(),page.getRecords()));
    }
}
