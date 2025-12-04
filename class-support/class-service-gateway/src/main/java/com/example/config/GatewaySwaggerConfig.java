package com.example.config;

import jakarta.annotation.PostConstruct;
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class GatewaySwaggerConfig {
    private final DiscoveryClient discoveryClient;
    private final SwaggerUiConfigProperties swaggerUiConfig;
    private final SwaggerUiConfigParameters swaggerUiConfigParameters; // 新增注入

    public GatewaySwaggerConfig(DiscoveryClient discoveryClient,
                                SwaggerUiConfigProperties swaggerUiConfig,
                                SwaggerUiConfigParameters swaggerUiConfigParameters) {
        this.discoveryClient = discoveryClient;
        this.swaggerUiConfig = swaggerUiConfig;
        this.swaggerUiConfigParameters = swaggerUiConfigParameters;
    }

    // 容器初始化完成后执行（确保服务发现已加载微服务列表）
    @PostConstruct
    public void dynamicAddSwaggerUrls() {
        // 1. 从注册中心获取所有微服务（排除 Gateway 自身）
        List<String> serviceIds = discoveryClient.getServices().stream()
                .filter(serviceId -> !"service-gateway".equals(serviceId)) // 替换为你的 Gateway 服务名
                .collect(Collectors.toList());

        Set<AbstractSwaggerUiConfigProperties.SwaggerUrl> swaggerUrls = new HashSet<>();
        // 2. 动态添加每个微服务的文档地址（与通用路由路径 /v3/api-docs/{serviceId} 对应）
        for (String serviceId : serviceIds) {
            SwaggerUiConfigProperties.SwaggerUrl url = new SwaggerUiConfigProperties.SwaggerUrl();
            url.setName(serviceId); // 下拉框显示的微服务名称（可改为中文，如 "订单服务"）
            url.setUrl("/swagger-doc/" + serviceId + "/v3/api-docs"); // 对应通用文档路由的路径
            swaggerUrls.add(url);
        }

        swaggerUiConfig.setUrls(swaggerUrls);
        swaggerUiConfigParameters.setUrls(swaggerUrls);
    }
}