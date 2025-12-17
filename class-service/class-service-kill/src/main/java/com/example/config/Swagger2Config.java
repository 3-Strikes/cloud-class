package com.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class Swagger2Config {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("秒杀服务")
                        .description("秒杀服务接口文档")
                        .version("v1.0.0")
                        .contact(new Contact().name("fyt").email("fythnu@163.com"))
                );
    }

}