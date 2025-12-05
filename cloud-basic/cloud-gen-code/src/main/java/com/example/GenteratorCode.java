package com.example;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.builder.CustomFile;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

import java.util.*;

/**
 * 生成代码的主类
 * 改 /com/example
 */
public class GenteratorCode {

    public static void main(String[] args) throws InterruptedException {
        ResourceBundle rb = ResourceBundle.getBundle("mybatiesplus-config-system"); //加载配置文件

        String[] tables = {
                "t_systemdictionaryitem",
                "t_systemdictionary",
                "t_operation_log",
                "t_employee",
                "t_department",
                "t_config"
        };

        FastAutoGenerator.create(rb.getString("jdbc.url"), rb.getString("jdbc.user"), rb.getString("jdbc.pwd"))
                .globalConfig(builder -> builder
                                .author(rb.getString("author"))
//                        .outputDir(rb.getString("OutputDir"))
                                .commentDate("yyyy-MM-dd")
                )
                .packageConfig(builder -> builder
                        .parent(rb.getString("parent"))
                        .controller("web.controller")
                        .entity("domain")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                        .xml("mappers")
                )
                .templateConfig(builder -> builder
                        .entity(null) // 关闭框架默认的 Entity 生成（只保留自定义的）
                        // 其他模板若已通过 customFile 自定义，也可设为 null，避免重复（可选）
                        .controller(null)
                        .service(null)
                        .serviceImpl(null)
                        .mapper(null)
                        .xml(null).build()
                )
                .injectionConfig(injectConfig -> {
                    Map<String,Object> customMap = new HashMap<>();
                    customMap.put("abc",rb.getString("author")+ "-rb");
                    injectConfig.customMap(customMap); //注入自定义属性

                    injectConfig.customFile(new CustomFile.Builder()
                            .fileName("Query.java") //文件名称
                            .filePath(rb.getString("OutputDirBase")+ "/com/example/query/")
                            .templatePath("/templates/query.java.vm") //指定生成模板路径
                            .build());

                    injectConfig.customFile(new CustomFile.Builder()
                            .fileName("Controller.java") //文件名称
                            .filePath(rb.getString("OutputDir")+ "/com/example/web/controller/")
                            .templatePath("/templates/controller.java.vm") //指定生成模板路径
                            .build());

                    injectConfig.customFile(new CustomFile.Builder()
                            .fileName("Service.java") //文件名称
                            .filePath(rb.getString("OutputDir")+ "/com/example/service/")
                            .templatePath("/templates/service.java.vm") //指定生成模板路径
                            .build());

                    injectConfig.customFile(new CustomFile.Builder()
                            .fileName("ServiceImpl.java") //文件名称
                            .filePath(rb.getString("OutputDir")+ "/com/example/service/impl")
                            .templatePath("/templates/serviceImpl.java.vm") //指定生成模板路径
                            .build());

                    injectConfig.customFile(new CustomFile.Builder()
                            .fileName("Mapper.java") //文件名称
                            .filePath(rb.getString("OutputDir")+ "/com/example/mapper")
                            .templatePath("/templates/mapper.java.vm") //指定生成模板路径
                            .build());

                    injectConfig.customFile(new CustomFile.Builder()
                            .fileName(".java") //文件名称
                            .filePath(rb.getString("OutputDirBase")+ "/com/example/domain/")
                            .templatePath("/templates/entity.java.vm") //指定生成模板路径
                            .build());

                    injectConfig.customFile(new CustomFile.Builder()
                            .fileName("Mapper.xml") //文件名称
                            .filePath(rb.getString("OutputDirXml")+ "/com/example/mapper/")
                            .templatePath("/templates/mapper.xml.vm") //指定生成模板路径
                            .build());

                }).strategyConfig(builder -> builder
                        .addTablePrefix("t_")
                        .addInclude(tables)
                        .enableCapitalMode() // 开启大写命名
                        .entityBuilder()
                        .enableTableFieldAnnotation()
                        .naming(NamingStrategy.underline_to_camel)
                        .columnNaming(NamingStrategy.underline_to_camel)
                        .serviceBuilder().formatServiceFileName("%sService")
                )
                .execute();

    }
}