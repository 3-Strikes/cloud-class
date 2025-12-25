# **Security知识点总结**

## 一、核心原理

​		1.过滤器链：请求需经过16个核心过滤器，关键过滤器顺序如下：
​				①：SecurityContextHolderFilter：设置安全上下文（存储用户认证信息）
​				②：CsrfFilter：CSRF防护（前后端分离可禁用）
​				③：UsernamePasswordAuthenticationFilter：表单登录认证
​				④：AuthorizationFilter：最终权限校验（判断是否允许访问资源）
​				⑤：ExceptionTranslationFilter：铺货认证/授权异常（跳转登录页或返回异常）

​		2.核心对象：
​				UsernamePasswordAuthenticationFilter：拦截/login请求，处理用户名密码登录（自定义登录需重写）
​				UserDetailService：查询用户信息的核心接口（必须自定义实现，从数据库/缓存获取用户）
​				UserDetails：封装用户详情（用户名，加密密码，权限集合），security内部使用的用户模型
​				AuthenticationManager：认证管理器（实现类ProviderManager），委托认证提供者执行校验
​				SecurityContextHolder：存储当前用户认证信息

​		**3.核心理解**：

- Spring Security 的核心是 “过滤器链 + 安全上下文”：请求经过过滤器链完成认证 / 授权，认证成功后将用户信息存入`SecurityContextHolder`，后续接口可直接获取。
- 核心对象的交互流程：用户提交账号密码 → 封装为`UsernamePasswordAuthenticationToken` → `AuthenticationManager`委托`AuthenticationProvider` → 调用`UserDetailsService`查询用户 → 密码编码器校验密码 → 认证成功更新上下文。