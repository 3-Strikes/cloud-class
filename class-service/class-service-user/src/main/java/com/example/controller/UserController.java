package com.example.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.cache.CacheService;
import com.example.constant.CacheKeys;
import com.example.domain.User;
import com.example.dto.RegisDTO;
import com.example.exceptions.BusinessException;
import com.example.query.UserQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.UserService;
import com.example.util.AssertUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    public UserService userService;

    @Autowired
    private CacheService cacheService;

    //dto对象：也是个实体类对象，但不对应数据库表，对应的前端表单参数结构
    @PostMapping("/register")
    public JSONResult register(@RequestBody RegisDTO regisInfo){
        //数据校验,非空校验
        AssertUtil.isNotEmpty(regisInfo.getMobile(),"手机号不能为空");
        AssertUtil.isPhone(regisInfo.getMobile(),"手机号格式不正确");
        AssertUtil.isNotEmpty(regisInfo.getPassword(),"密码不能为空");
        AssertUtil.isNotEmpty(regisInfo.getSmsCode(),"验证码不能为空");

        String mobile = regisInfo.getMobile();
        String key= CacheKeys.VALI_CODE+mobile;
        Object o = cacheService.get(key);
        if(o==null)throw new BusinessException("验证码已过期");
        Map<String, Object> data = (Map<String, Object>) o;
        if(!regisInfo.getSmsCode().equals(data.get("code")))throw new BusinessException("验证码错误");

        //手机号是否已注册（phone列，应该加索引，提高查询效率）
        User one = userService.getOne(Wrappers.lambdaQuery(User.class).eq(User::getPhone, mobile));
        if(one!=null)throw new BusinessException("手机号已注册");

        userService.regis(regisInfo);
        return JSONResult.success(userService.list());
    }

    /**
    * 保存和修改公用的
    */
    @RequestMapping(value="/save",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody User user){
        if(user.getId()!=null){
            userService.updateById(user);
        }else{
            userService.save(user);
        }
        return JSONResult.success();
    }

    /**
    * 删除对象
    */
    @RequestMapping(value="/{id}",method=RequestMethod.DELETE)
    public JSONResult delete(@PathVariable("id") Long id){
        userService.removeById(id);
        return JSONResult.success();
    }

    /**
   * 获取对象
   */
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public JSONResult get(@PathVariable("id")Long id){
        return JSONResult.success(userService.getById(id));
    }


    /**
    * 查询所有对象
    */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public JSONResult list(){
        return JSONResult.success(userService.list());
    }


    /**
    * 带条件分页查询数据
    */
    @RequestMapping(value = "/pagelist",method = RequestMethod.POST)
    public JSONResult page(@RequestBody UserQuery query){
        Page<User> page = new Page<User>(query.getPage(),query.getRows());
        page = userService.page(page);
        return JSONResult.success(new PageList<User>(page.getTotal(),page.getRecords()));
    }
}
