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
 * @author lzy
 * @since 2025-12-08
 */
@TableName("t_course_user_learn")
public class CourseUserLearn implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("login_id")
    private Long loginId;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 0已购买，1已过期
     */
    @TableField("state")
    private Byte state;

    @TableField("course_id")
    private Long courseId;

    @TableField("course_order_no")
    private String courseOrderNo;

    @TableField("create_time")
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLoginId() {
        return loginId;
    }

    public void setLoginId(Long loginId) {
        this.loginId = loginId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseOrderNo() {
        return courseOrderNo;
    }

    public void setCourseOrderNo(String courseOrderNo) {
        this.courseOrderNo = courseOrderNo;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "CourseUserLearn{" +
            "id = " + id +
            ", loginId = " + loginId +
            ", startTime = " + startTime +
            ", endTime = " + endTime +
            ", state = " + state +
            ", courseId = " + courseId +
            ", courseOrderNo = " + courseOrderNo +
            ", createTime = " + createTime +
            "}";
    }
}
