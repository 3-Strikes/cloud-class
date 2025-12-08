package com.example.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

/**
 * <p>
 * 老师表
 * </p>
 *
 * @author lzy
 * @since 2025-12-08
 */
@TableName("t_teacher")
public class Teacher implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    /**
     * 简介
     */
    @TableField("intro")
    private String intro;

    /**
     * 技术栈
     */
    @TableField("technology")
    private String technology;

    /**
     * 职位，高级讲师，架构师
     */
    @TableField("position")
    private String position;

    /**
     * 头像
     */
    @TableField("head_img")
    private String headImg;

    @TableField("tags")
    private String tags;

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

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "Teacher{" +
            "id = " + id +
            ", name = " + name +
            ", intro = " + intro +
            ", technology = " + technology +
            ", position = " + position +
            ", headImg = " + headImg +
            ", tags = " + tags +
            "}";
    }
}
