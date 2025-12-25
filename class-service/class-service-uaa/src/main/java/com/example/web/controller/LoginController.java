package com.example.web.controller;

import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.Login;
import com.example.query.LoginQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    public LoginService loginService;

    @Autowired
    private OAuth2ResourceServerProperties oAuth2ResourceServerProperties;
    @RequestMapping(value="/refresh",method= RequestMethod.POST)
    public JSONResult saveOrUpdate(@RequestBody Map map){
        String refreshToken = (String)map.get("refreshToken");
        String username = (String)map.get("username");
        Login one = loginService.getOne(Wrappers.lambdaQuery(Login.class).eq(Login::getUsername, username));

        String url=oAuth2ResourceServerProperties.getJwt().getIssuerUri()+"/oauth2/token";

        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("grant_type","refresh_token");
        paramMap.put("client_id",one.getClientId());
        paramMap.put("client_secret",one.getClientSecret());
        paramMap.put("refresh_token",refreshToken);

        String post = HttpUtil.post(url, paramMap);
        return JSONResult.success(post);
    }


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
