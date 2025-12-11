package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.MessageEmail;
import com.example.query.MessageEmailQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.MessageEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messageEmail")
public class MessageEmailController {

    @Autowired
    public MessageEmailService messageEmailService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody MessageEmail messageEmail){
        if(messageEmail.getId()!=null){
            messageEmailService.updateById(messageEmail);
        }else{
            messageEmailService.save(messageEmail);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        messageEmailService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(messageEmailService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(messageEmailService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody MessageEmailQuery query){
        Page<MessageEmail> page = new Page<MessageEmail>(query.getPage(),query.getRows());
        page = messageEmailService.page(page);
        return JSONResult.success(new PageList<MessageEmail>(page.getTotal(),page.getRecords()));
    }
}
