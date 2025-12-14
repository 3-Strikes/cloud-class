package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.PayOrder;
import com.example.query.PayOrderQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.PayOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payOrder")
public class PayOrderController {

    @Autowired
    public PayOrderService payOrderService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody PayOrder payOrder){
        if(payOrder.getId()!=null){
            payOrderService.updateById(payOrder);
        }else{
            payOrderService.save(payOrder);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        payOrderService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(payOrderService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(payOrderService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody PayOrderQuery query){
        Page<PayOrder> page = new Page<PayOrder>(query.getPage(),query.getRows());
        page = payOrderService.page(page);
        return JSONResult.success(new PageList<PayOrder>(page.getTotal(),page.getRecords()));
    }
}
