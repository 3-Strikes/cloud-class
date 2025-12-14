package com.example.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author fyt
 * @since 2025-12-13
 */
@TableName("t_course_order_item")
public class CourseOrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 对应秒杀订单
     */
    @TableField("order_id")
    private String orderId;

    /**
     * 秒杀课程的价格
     */
    @TableField("amount")
    private Double amount;

    /**
     * 秒杀到课程的数量
     */
    @TableField("count")
    private Integer count;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    /**
     * 秒杀课程ID，不是课程ID
     */
    @TableField("course_id")
    private Long courseId;

    /**
     * 秒杀课程名字
     */
    @TableField("course_name")
    private String courseName;

    /**
     * 封面
     */
    @TableField("course_pic")
    private String coursePic;

    @TableField("version")
    private Integer version;

    @TableField("order_no")
    private String orderNo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
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

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCoursePic() {
        return coursePic;
    }

    public void setCoursePic(String coursePic) {
        this.coursePic = coursePic;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    @Override
    public String toString() {
        return "CourseOrderItem{" +
            "id = " + id +
            ", orderId = " + orderId +
            ", amount = " + amount +
            ", count = " + count +
            ", createTime = " + createTime +
            ", updateTime = " + updateTime +
            ", courseId = " + courseId +
            ", courseName = " + courseName +
            ", coursePic = " + coursePic +
            ", version = " + version +
            ", orderNo = " + orderNo +
            "}";
    }
}
