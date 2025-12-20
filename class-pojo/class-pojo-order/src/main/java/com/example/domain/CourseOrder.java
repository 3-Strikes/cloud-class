package com.example.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author fyt
 * @since 2025-12-13
 */
@TableName("t_course_order")
public class CourseOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 最后支付更新时间
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * 订单编号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 支付总的价格
     */
    @TableField("total_amount")
    private Double totalAmount;

    /**
     * 秒杀数量
     */
    @TableField("total_count")
    private Integer totalCount;

    /**
     *     // 订单状态 ：	    //0下单成功待支付，	    //1支付成功订单完成	    //2用户手动取消订单(未支付)	    //3.支付失败	    //4.超时自动订单取消
     */
    @TableField("status_order")
    private Integer statusOrder;

    /**
     * 用户
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 订单标题
     */
    @TableField("title")
    private String title;

    @TableField("version")
    private Integer version;

    /**
     * 支付方式:0余额直接，1支付宝，2微信,3银联
     */
    @TableField("pay_type")
    private Integer payType;

    @TableField(exist = false)
    private List<CourseOrderItem> items;

    @TableField(exist = false)
    private Integer isKill; // 是否为秒杀订单：1-是，0-否
    @TableField(exist = false)
    private Long actId; // 秒杀活动ID（关联秒杀活动表）

    public Integer getIsKill() {
        return isKill;
    }

    public void setIsKill(Integer isKill) {
        this.isKill = isKill;
    }

    public Long getActId() {
        return actId;
    }

    public void setActId(Long actId) {
        this.actId = actId;
    }

    public List<CourseOrderItem> getItems() {
        return items;
    }

    public void setItems(List<CourseOrderItem> items) {
        this.items = items;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getStatusOrder() {
        return statusOrder;
    }

    public void setStatusOrder(Integer statusOrder) {
        this.statusOrder = statusOrder;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    @Override
    public String toString() {
        return "CourseOrder{" +
            "id = " + id +
            ", createTime = " + createTime +
            ", updateTime = " + updateTime +
            ", orderNo = " + orderNo +
            ", totalAmount = " + totalAmount +
            ", totalCount = " + totalCount +
            ", statusOrder = " + statusOrder +
            ", userId = " + userId +
            ", title = " + title +
            ", version = " + version +
            ", payType = " + payType +
            "}";
    }
}
