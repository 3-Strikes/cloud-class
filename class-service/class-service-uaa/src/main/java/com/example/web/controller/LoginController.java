package com.example.web.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.Login;
import com.example.query.LoginQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.LoginService;
import com.example.util.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;
    
    /**
     * 前台用户登录接口（直接返回token，不需要OAuth2授权码流程）
     * 流程：1. 验证用户 2. 生成tmpToken 3. 使用tmpToken调用/oauth2/token获取token
     */
//    @RequestMapping(value="/common",method= RequestMethod.POST)
//    public JSONResult commonLogin(@RequestBody Login loginParam){
//        try {
//            // 1. 参数校验
//            if(StrUtil.isEmpty(loginParam.getUsername())){
//                return JSONResult.error("用户名不能为空");
//            }
//            if(StrUtil.isEmpty(loginParam.getPassword())){
//                return JSONResult.error("密码不能为空");
//            }
//            if(loginParam.getType() == null){
//                return JSONResult.error("用户类型不能为空");
//            }
//
//            // 2. 查询用户
//            Login login = loginService.getOne(Wrappers.lambdaQuery(Login.class)
//                    .eq(Login::getUsername, loginParam.getUsername())
//                    .eq(Login::getType, loginParam.getType()));
//
//            if(login == null){
//                return JSONResult.error("用户不存在");
//            }
//
//            // 3. 验证密码
//            String encryptedPassword = MD5Utils.encrypt32(loginParam.getPassword());
////            String encryptedPassword = loginParam.getPassword();
//            if(!encryptedPassword.equals(login.getPassword())){
//                return JSONResult.error("密码错误");
//            }
//
//            // 4. 进行用户认证（复用现有的认证逻辑）
//            Login authParam = new Login();
//            authParam.setUsername(loginParam.getUsername());
//            authParam.setPassword(loginParam.getPassword());
//            authParam.setType(loginParam.getType());
//
//            String authParamJson = JSON.toJSONString(authParam);
//            UsernamePasswordAuthenticationToken authRequest =
//                new UsernamePasswordAuthenticationToken(loginParam.getUsername(), loginParam.getPassword());
//
//            Authentication authentication;
//            try {
//                authentication = authenticationConfiguration.getAuthenticationManager().authenticate(authRequest);
//            } catch (Exception e) {
//                return JSONResult.error("认证失败: " + e.getMessage());
//            }
//
//            // 5. 生成临时token（用于OAuth2 token请求）
//            org.springframework.security.core.userdetails.User user =
//                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
//            String username = user.getUsername(); // 用户信息JSON字符串
//            String tmpToken = com.example.utils.JwtTokenUtils.generateToken(username, 2*60*1000L);
//
//            // 6. 调用OAuth2 token接口获取access_token和refresh_token
//            String tokenUrl = oAuth2ResourceServerProperties.getJwt().getIssuerUri() + "/oauth2/token";
//            String redirectUri = "http://127.0.0.1:6003/callback";
//
//            Map<String,Object> tokenParamMap = new HashMap<>();
//            tokenParamMap.put("grant_type", "authorization_code");
//            // 注意：这里需要一个真实的授权码，但为了简化流程，我们使用tmpToken
//            // OAuth2 token接口会通过MyTmpTokenCheckFilter验证tmpToken并设置认证上下文
//            tokenParamMap.put("code", "common_login_code"); // 特殊标识，后端需要特殊处理
//            tokenParamMap.put("client_id", login.getClientId());
//            tokenParamMap.put("client_secret", login.getClientSecret());
//            tokenParamMap.put("redirect_uri", redirectUri);
//            tokenParamMap.put("tempToken", tmpToken);
//
//            String tokenResponse = HttpUtil.post(tokenUrl, tokenParamMap);
//
//            // 7. 解析返回的token
//            Map<String, Object> tokenMap = JSON.parseObject(tokenResponse, Map.class);
//
//            // 如果返回的是错误信息
//            if(tokenMap.containsKey("error")){
//                return JSONResult.error("获取token失败: " + tokenMap.get("error_description"));
//            }
//
//            return JSONResult.success(tokenMap);
//        } catch (Exception e) {
//            return JSONResult.error("登录失败: " + e.getMessage());
//        }
//    }
    
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
