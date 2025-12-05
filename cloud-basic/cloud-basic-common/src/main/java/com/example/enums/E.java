package com.example.enums;

public enum E {
    SUC("20000","操作成功"),
    ERROR("50000","操作失败"),

    PWD_ERROR("50001","密码错误"),
    USER_NOT_EXISTS("50002","用户名不存在"),
    PHONE_EXISTS("50003","手机号已存在"),
    ARG_VALI_ERROR("50004","参数校验失败"),
    ;
    private String code;
    private String msg;

    E(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
