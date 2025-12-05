package com.example.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 会员基本信息
 * </p>
 *
 * @author fyt
 * @since 2025-12-05
 */
@TableName("t_user_base_info")
public class UserBaseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    private Long id;

    @TableField("create_time")
    private Long createTime;

    @TableField("update_time")
    private Long updateTime;

    /**
     * 注册渠道
     */
    @TableField("reg_channel")
    private Byte regChannel;

    /**
     * QQ
     */
    @TableField("qq")
    private String qq;

    /**
     * 用户等级
     */
    @TableField("level")
    private Byte level;

    /**
     * 成长值
     */
    @TableField("grow_score")
    private Integer growScore;

    /**
     * 推荐人
     */
    @TableField("refer_id")
    private Long referId;

    /**
     * 性别
     */
    @TableField("sex")
    private Byte sex;

    /**
     * 生日
     */
    @TableField("birthday")
    private LocalDate birthday;

    @TableField("area_code")
    private String areaCode;

    @TableField("address")
    private String address;

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

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Byte getRegChannel() {
        return regChannel;
    }

    public void setRegChannel(Byte regChannel) {
        this.regChannel = regChannel;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public Byte getLevel() {
        return level;
    }

    public void setLevel(Byte level) {
        this.level = level;
    }

    public Integer getGrowScore() {
        return growScore;
    }

    public void setGrowScore(Integer growScore) {
        this.growScore = growScore;
    }

    public Long getReferId() {
        return referId;
    }

    public void setReferId(Long referId) {
        this.referId = referId;
    }

    public Byte getSex() {
        return sex;
    }

    public void setSex(Byte sex) {
        this.sex = sex;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "UserBaseInfo{" +
            "id = " + id +
            ", createTime = " + createTime +
            ", updateTime = " + updateTime +
            ", regChannel = " + regChannel +
            ", qq = " + qq +
            ", level = " + level +
            ", growScore = " + growScore +
            ", referId = " + referId +
            ", sex = " + sex +
            ", birthday = " + birthday +
            ", areaCode = " + areaCode +
            ", address = " + address +
            "}";
    }
}
