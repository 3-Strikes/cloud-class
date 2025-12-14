package com.example.enums;

public enum OrderStatus {
    TO_PAY(0,"待支付"),
    PAY_SUC(1,"支付完成"),
    MANUAL_CANCEL(2,"手动取消订单"),
    PAY_FAIL(3,"支付失败"),
    TIMEOUT_CANCEL(4,"超时取消"),
    ;
    private Integer code;
    private String desc;

    OrderStatus(Integer code, String desc) {
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
