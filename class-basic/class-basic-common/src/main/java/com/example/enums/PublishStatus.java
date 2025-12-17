package com.example.enums;

public enum PublishStatus {
    TO_PUBLISH(0,"待发布"),
    PUBLISH_SUC(1,"已发布"),
    CANCEL_PUBLISH(2,"已取消"),
    ;
    private Integer code;
    private String desc;

    PublishStatus(Integer code, String desc) {
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
