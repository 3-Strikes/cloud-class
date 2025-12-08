package com.example.enums;

public enum UserType {
    WEB_SITE(1,"website"),
    ADMIN_EMPLOYEE(0,"backstage"),
    ;
    private Integer code;
    private String desc;

    UserType(Integer code, String desc) {
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
