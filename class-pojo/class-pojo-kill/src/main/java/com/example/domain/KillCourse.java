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
 * @since 2025-12-17
 */
@TableName("t_kill_course")
public class KillCourse implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 课程名字
     */
    @TableField("course_name")
    private String courseName;

    /**
     * 对应的课程ID
     */
    @TableField("course_id")
    private Long courseId;

    @TableField("kill_price")
    private BigDecimal killPrice;

    /**
     * 库存
     */
    @TableField("kill_count")
    private Integer killCount;

    /**
     * 每个人可以秒杀的数量,默认1
     */
    @TableField("kill_limit")
    private Integer killLimit;

    /**
     * 秒杀课程排序
     */
    @TableField("kill_sort")
    private Integer killSort;

    /**
     * 秒杀状态:0待发布，1秒杀中，2秒杀结束
     */
    @TableField("publish_status")
    private Integer publishStatus;

    @TableField("course_pic")
    private String coursePic;

    /**
     * 秒杀开始时间
     */
    @TableField("start_time")
    private Date startTime;

    /**
     * 秒杀结束时间
     */
    @TableField("end_time")
    private Date endTime;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 发布到Redis的时间
     */
    @TableField("publish_time")
    private Date publishTime;

    /**
     * 老师，用逗号隔开
     */
    @TableField("teacher_names")
    private String teacherNames;

    /**
     * 下线时间
     */
    @TableField("offline_time")
    private Date offlineTime;

    @TableField("activity_id")
    private Long activityId;

    @TableField("time_str")
    private String timeStr;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public BigDecimal getKillPrice() {
        return killPrice;
    }

    public void setKillPrice(BigDecimal killPrice) {
        this.killPrice = killPrice;
    }

    public Integer getKillCount() {
        return killCount;
    }

    public void setKillCount(Integer killCount) {
        this.killCount = killCount;
    }


    public Integer getKillSort() {
        return killSort;
    }

    public void setKillSort(Integer killSort) {
        this.killSort = killSort;
    }

    public Integer getKillLimit() {
        return killLimit;
    }

    public void setKillLimit(Integer killLimit) {
        this.killLimit = killLimit;
    }

    public Integer getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(Integer publishStatus) {
        this.publishStatus = publishStatus;
    }

    public String getCoursePic() {
        return coursePic;
    }

    public void setCoursePic(String coursePic) {
        this.coursePic = coursePic;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public String getTeacherNames() {
        return teacherNames;
    }

    public void setTeacherNames(String teacherNames) {
        this.teacherNames = teacherNames;
    }

    public Date getOfflineTime() {
        return offlineTime;
    }

    public void setOfflineTime(Date offlineTime) {
        this.offlineTime = offlineTime;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getTimeStr() {
        return timeStr;
    }

    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr;
    }
}
