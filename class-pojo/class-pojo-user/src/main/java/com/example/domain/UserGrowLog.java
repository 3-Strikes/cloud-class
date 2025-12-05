package com.example.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

/**
 * <p>
 * 成长值记录
 * </p>
 *
 * @author fyt
 * @since 2025-12-05
 */
@TableName("t_user_grow_log")
public class UserGrowLog implements Serializable {

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
     * 来源
     */
    @TableField("from_reason")
    private String fromReason;

    /**
     * 成长值
     */
    @TableField("score")
    private Integer score;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

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

    public String getFromReason() {
        return fromReason;
    }

    public void setFromReason(String fromReason) {
        this.fromReason = fromReason;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "UserGrowLog{" +
            "id = " + id +
            ", createTime = " + createTime +
            ", userId = " + userId +
            ", fromReason = " + fromReason +
            ", score = " + score +
            ", remark = " + remark +
            "}";
    }
}
