package com.example.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CourseOrderDTO {
    @NotEmpty(message = "课程id不能为空")
    private List<Long> courseIds;
    @NotNull(message="支付类型不能为空")
    private Integer payType;
    @NotEmpty(message = "token不能为空")
   private String token;

    private Integer type;

    @NotEmpty(message = "秒杀临时订单号不能为空") // 秒杀场景必传
    private String orderNo; // 临时订单号（秒杀阶段生成）

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public List<Long> getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(List<Long> courseIds) {
        this.courseIds = courseIds;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
