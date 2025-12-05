package com.example.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

/**
 * <p>
 * 登录记录
 * </p>
 *
 * @author fyt
 * @since 2025-12-05
 */
@TableName("t_login_log")
public class LoginLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("create_time")
    private Long createTime;

    @TableField("login_id")
    private Long loginId;

    /**
     * IP
     */
    @TableField("ip")
    private String ip;

    /**
     * 客户端
     */
    @TableField("client_info")
    private String clientInfo;

    /**
     * 登录方式
     */
    @TableField("login_type")
    private Byte loginType;

    /**
     * 登录是否成功
     */
    @TableField("success")
    private Byte success;

    /**
     * 结果说明
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

    public Long getLoginId() {
        return loginId;
    }

    public void setLoginId(Long loginId) {
        this.loginId = loginId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(String clientInfo) {
        this.clientInfo = clientInfo;
    }

    public Byte getLoginType() {
        return loginType;
    }

    public void setLoginType(Byte loginType) {
        this.loginType = loginType;
    }

    public Byte getSuccess() {
        return success;
    }

    public void setSuccess(Byte success) {
        this.success = success;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "LoginLog{" +
            "id = " + id +
            ", createTime = " + createTime +
            ", loginId = " + loginId +
            ", ip = " + ip +
            ", clientInfo = " + clientInfo +
            ", loginType = " + loginType +
            ", success = " + success +
            ", remark = " + remark +
            "}";
    }
}
