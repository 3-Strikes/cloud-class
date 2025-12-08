package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.UserAddress;
import com.example.query.UserAddressQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/userAddress")
public class UserAddressController {

    @Autowired
    public UserAddressService userAddressService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody UserAddress userAddress){
        if(userAddress.getId()!=null){
            userAddressService.updateById(userAddress);
        }else{
            userAddressService.save(userAddress);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        userAddressService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(userAddressService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(userAddressService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody UserAddressQuery query){
        Page<UserAddress> page = new Page<UserAddress>(query.getPage(),query.getRows());
        page = userAddressService.page(page);
        return JSONResult.success(new PageList<UserAddress>(page.getTotal(),page.getRecords()));
    }
}
