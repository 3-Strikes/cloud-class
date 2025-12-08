package com.example.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 
 * </p>
 *
 * @author lzy
 * @since 2025-12-08
 */
@TableName("t_course_market")
public class CourseMarket implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程id
     */
    @TableId("id")
    private Long id;

    /**
     * 收费规则：，收费1免费，2收费
     */
    @TableField("charge")
    private Byte charge;

    /**
     * 咨询qq
     */
    @TableField("qq")
    private String qq;

    /**
     * 价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 原价
     */
    @TableField("price_old")
    private BigDecimal priceOld;

    @TableField("valid_days")
    private Integer validDays;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Byte getCharge() {
        return charge;
    }

    public void setCharge(Byte charge) {
        this.charge = charge;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPriceOld() {
        return priceOld;
    }

    public void setPriceOld(BigDecimal priceOld) {
        this.priceOld = priceOld;
    }

    public Integer getValidDays() {
        return validDays;
    }

    public void setValidDays(Integer validDays) {
        this.validDays = validDays;
    }

    @Override
    public String toString() {
        return "CourseMarket{" +
            "id = " + id +
            ", charge = " + charge +
            ", qq = " + qq +
            ", price = " + price +
            ", priceOld = " + priceOld +
            ", validDays = " + validDays +
            "}";
    }
}
