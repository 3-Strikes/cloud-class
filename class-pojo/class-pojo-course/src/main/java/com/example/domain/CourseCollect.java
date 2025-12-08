package com.example.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

/**
 * <p>
 * 商品收藏
 * </p>
 *
 * @author lzy
 * @since 2025-12-08
 */
@TableName("t_course_collect")
public class CourseCollect implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("create_time")
    private Long createTime;

    /**
     * 登录用户
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 商品ID
     */
    @TableField("course_id")
    private Long courseId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    @Override
    public String toString() {
        return "CourseCollect{" +
            "id = " + id +
            ", createTime = " + createTime +
            ", userId = " + userId +
            ", courseId = " + courseId +
            "}";
    }
}
