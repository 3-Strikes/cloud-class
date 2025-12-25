package com.example.security;

import com.alibaba.fastjson.JSON;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.example.enums.E;
import com.example.result.JSONResult;
import com.example.utils.JwtTokenUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UaaUserDetailService uaaUserDetailService;

    private MyUsernamePasswordAuthenticationFilter myUsernamePasswordAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //配置认证提供者，内部调用UserDetailsService的loadUserByUsername方法查询用户，然后进行密码校验
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(uaaUserDetailService); // 对接自定义用户数据
        authProvider.setPasswordEncoder(passwordEncoder()); // 密码加密规则
        return authProvider;
    }

    @Bean
    public MyUsernamePasswordAuthenticationFilter myUsernamePasswordAuthenticationFilter(AuthenticationConfiguration authenticationConfiguration,RegisteredClientRepository jdbcRegisteredClientRepository) throws Exception {
        myUsernamePasswordAuthenticationFilter = new MyUsernamePasswordAuthenticationFilter();
        myUsernamePasswordAuthenticationFilter.setAuthenticationManager(authenticationConfiguration.getAuthenticationManager());
        myUsernamePasswordAuthenticationFilter.setAuthenticationSuccessHandler((request, response, authentication) -> {
            response.setContentType("application/json;charset=UTF-8");
            User user = (User)authentication.getPrincipal();
            String username = user.getUsername();//用户信息,有id，有username，有type
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();//权限集合
//            RegisteredClient admin = jdbcRegisteredClientRepository.findByClientId("admin");

            //TODO 自己生成一个临时token，1分钟有效期，返回到前端，后续oauth请求，获取授权码，获取token使用。
            //解析token过滤器，根据token再生成 Authentication对象。放在SecurityContextHolder
            String tmpToken=JwtTokenUtils.generateToken(username,2*60*1000L);

            Map<String,String> data = new HashMap<>();
            data.put("tmpToken",tmpToken);
            data.put("loginUser",username);
            JSONResult success = JSONResult.success(data);
            response.getWriter().write(JSON.toJSONString(success));
        });
        myUsernamePasswordAuthenticationFilter.setAuthenticationFailureHandler((request, response, exception) -> {
            response.setContentType("application/json;charset=UTF-8");
            JSONResult error = JSONResult.error(exception.getMessage());
            response.getWriter().write(JSON.toJSONString(error));
        });

        myUsernamePasswordAuthenticationFilter.setFilterProcessesUrl("/login");
        return myUsernamePasswordAuthenticationFilter;
    }

    //springsecurity过滤器链配置
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
            throws Exception {
        http.formLogin(from -> from.disable());
        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.addFilterAt(myUsernamePasswordAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/login", "/oauth2/authorize", "/oauth2/token", "/oauth2/jwks").permitAll()  //无需登录可以访问
                .anyRequest().authenticated()
        );
         http.oauth2ResourceServer((oauth2) -> oauth2
                .jwt(Customizer.withDefaults())
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        response.setContentType("application/json;charset=utf-8");
                        response.getWriter().write(JSON.toJSONString(JSONResult.error(E.NOPER)));
                    }
                })
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        if(authException instanceof InsufficientAuthenticationException){
                            String accept=request.getHeader("accept");
                            if(accept.contains(MediaType.TEXT_HTML_VALUE)){
                                LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint=new LoginUrlAuthenticationEntryPoint("/login");
                                loginUrlAuthenticationEntryPoint.commence(request,response,authException);
                            }else{
                                response.setContentType("application/json;charset=utf-8");
                                response.getWriter().write(JSON.toJSONString(JSONResult.error(E.TOLOGIN)));
                            }
                        }else{
                            response.setContentType("application/json;charset=utf-8");
                            response.getWriter().write(JSON.toJSONString(JSONResult.error(E.TOLOGIN)));
                        }
                    }
                })
        );
        return http.build();
    }

    //授权服务器过滤器链配置
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http,MyTmpTokenCheckFilter myTmpTokenCheckFilter)
            throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults());
        //禁用session
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
             .csrf(csrf -> csrf.disable());
        http.addFilterBefore(myTmpTokenCheckFilter, WebAsyncManagerIntegrationFilter.class);
        http
                //未认证异常处理，转到login进行认证
                .exceptionHandling((exceptions) -> exceptions
//                        .defaultAuthenticationEntryPointFor(
//                                new LoginUrlAuthenticationEntryPoint("http://localhost:6001/login"),
//                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
//                        )
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (!MediaType.TEXT_HTML_VALUE.equals(request.getContentType())) {
                                response.setContentType("application/json;charset=utf-8");
                                response.getWriter().write(JSON.toJSONString(JSONResult.error(E.TOLOGIN)));
                            }
                        })
                )
                //当前应用也可以作为资源服务器，校验客户端传入的access_token的合法性
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        JdbcRegisteredClientRepository jdbcRegisteredClientRepository = new JdbcRegisteredClientRepository(jdbcTemplate);

        return jdbcRegisteredClientRepository;
    }

    //授权信息
    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository){
        JdbcOAuth2AuthorizationService jdbcOAuth2AuthorizationService = new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
        return jdbcOAuth2AuthorizationService;
    }

    //授权确认
    @Bean
    public OAuth2AuthorizationConsentService auth2AuthorizationConsentService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository){
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate,registeredClientRepository);
    }

    //生成与解析jwt的对象,使用私钥生成access_token,客户端或者资源服务器可以通过jwk接口获取公钥验证token合法性
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    //生成RSA算法的密钥对
    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

    //依赖JWKSource对象进行token的解析和验证
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    //授权服务器的全局配置项（各端口地址：获取授权码，获取token，获取公钥等），这里使用默认配置
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().issuer("http://127.0.0.1:10010/ymcc/uaa").build();
    }

    //自定义token内容
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return context -> {
            // 1. 获取当前认证的用户信息（仅在用户授权模式下有效，客户端凭证模式无用户）
            Authentication authentication = context.getPrincipal();
            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                // 2. 提取用户的所有权限（包含roles和authorities）
                List<String> authorities = userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList());

                // 3. 提取角色（过滤出以ROLE_开头的权限）
                List<String> roles = authorities.stream()
                        .filter(auth -> auth.startsWith("ROLE_"))
                        .collect(Collectors.toList());

                // 4. 提取纯权限（排除角色）
                List<String> permissions = authorities.stream()
                        .filter(auth -> !auth.startsWith("ROLE_"))
                        .collect(Collectors.toList());

                // 5. 向JWT中添加自定义声明
                context.getClaims()
                        // 用户名（默认已有sub，但可自定义）
                        .claim("username", userDetails.getUsername())
                        // 角色列表
                        .claim("roles", roles)
                        // 权限列表
                        .claim("permissions", permissions)
                        // 所有权限（包含角色+权限）
                        .claim("authorities", authorities);
            }
        };
    }
}