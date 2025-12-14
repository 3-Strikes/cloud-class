package com.example.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author fyt
 * @since 2025-12-13
 */
@TableName("t_pay_order")
public class PayOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 流水创建
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 发生金额
     */
    @TableField("amount")
    private Long amount;

    /**
     * 支付方式:0余额直接，1支付宝，2微信,3银联
     */
    @TableField("pay_type")
    private Byte payType;

    /**
     * 业务ID，可以关联订单ID,或者课程ID
     */
    @TableField("relation_id")
    private Long relationId;

    /**
     * 订单号
     */
    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Long userId;

    /**
     * 扩展参数，格式： xx=1&oo=2
     */
    @TableField("ext_params")
    private String extParams;

    /**
     * 描述
     */
    @TableField("subject")
    private String subject;

    /**
     * 支付状态
     */
    @TableField("pay_status")
    private Byte payStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Byte getPayType() {
        return payType;
    }

    public void setPayType(Byte payType) {
        this.payType = payType;
    }

    public Long getRelationId() {
        return relationId;
    }

    public void setRelationId(Long relationId) {
        this.relationId = relationId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getExtParams() {
        return extParams;
    }

    public void setExtParams(String extParams) {
        this.extParams = extParams;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Byte getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(Byte payStatus) {
        this.payStatus = payStatus;
    }

    @Override
    public String toString() {
        return "PayOrder{" +
            "id = " + id +
            ", createTime = " + createTime +
            ", updateTime = " + updateTime +
            ", amount = " + amount +
            ", payType = " + payType +
            ", relationId = " + relationId +
            ", orderNo = " + orderNo +
            ", userId = " + userId +
            ", extParams = " + extParams +
            ", subject = " + subject +
            ", payStatus = " + payStatus +
            "}";
    }
}
