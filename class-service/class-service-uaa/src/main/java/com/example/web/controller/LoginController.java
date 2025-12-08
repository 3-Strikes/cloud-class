package com.example.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.Login;
import com.example.query.LoginQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    public LoginService loginService;



    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody Login login){
        if(login.getId()!=null){
            loginService.updateById(login);
        }else{
            loginService.save(login);//mybatisplus在保存成功后，把自增id回写到login对象中
        }
        return JSONResult.success(login.getId());
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        loginService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(loginService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(loginService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody LoginQuery query){
        Page<Login> page = new Page<Login>(query.getPage(),query.getRows());
        page = loginService.page(page);
        return JSONResult.success(new PageList<Login>(page.getTotal(),page.getRecords()));
    }
}
