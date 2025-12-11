package com.example.enums;


public enum CourseStatus {
    UN_LINE(0,"下架"),
    ON_LINE(1,"上架"),
    ;
    private Integer code;
    private String desc;

    CourseStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
