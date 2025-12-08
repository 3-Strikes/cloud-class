package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.UserAccount;
import com.example.query.UserAccountQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.UserAccountService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/userAccount")
public class UserAccountController {

    @Autowired
    public UserAccountService userAccountService;

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody UserAccount userAccount){
        if(userAccount.getId()!=null){
            userAccountService.updateById(userAccount);
        }else{
            userAccountService.save(userAccount);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        userAccountService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(userAccountService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(userAccountService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody UserAccountQuery query){
        Page<UserAccount> page = new Page<UserAccount>(query.getPage(),query.getRows());
        page = userAccountService.page(page);
        return JSONResult.success(new PageList<UserAccount>(page.getTotal(),page.getRecords()));
    }
}
