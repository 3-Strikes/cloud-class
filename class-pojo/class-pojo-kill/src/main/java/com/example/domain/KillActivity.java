package com.example.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author fyt
 * @since 2025-12-17
 */
@TableName("t_kill_activity")
public class KillActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("time_str")
    private String timeStr;

    @TableField("begin_time")
    private Date beginTime;

    @TableField("end_time")
    private Date endTime;

    /**
     * 0待发布，1已发布 ，2已取消
     */
    @TableField("publish_status")
    private Integer publishStatus;

    @TableField("publish_time")
    private Date publishTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimeStr() {
        return timeStr;
    }

    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(Integer publishStatus) {
        this.publishStatus = publishStatus;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    @Override
    public String toString() {
        return "KillActivity{" +
            "id = " + id +
            ", name = " + name +
            ", timeStr = " + timeStr +
            ", beginTime = " + beginTime +
            ", endTime = " + endTime +
            ", publishStatus = " + publishStatus +
            ", publishTime = " + publishTime +
            "}";
    }
}
