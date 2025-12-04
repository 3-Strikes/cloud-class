package com.example.query;


import lombok.Data;

/**
 * 基础查询对象
 */
@Data
public class BaseQuery {
    //有公共属性-分页
    private Integer page = 1; //当前页

    private Integer rows = 10; //每页显示多少条

    //查询关键字
    private String keyword;
    //排序的字段
    private String sortField;
    //排序的方式：desc ; asc
    private String sortType;
}
