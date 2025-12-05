package com.example.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 员工实体类
 *
 * @author lzy
 * @since 2025-12-04
 */
@Data // 替代手动get/set，简化代码
@TableName("t_employee")
@Schema(description = "员工实体")
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空") // 参数校验
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z]{2,20}$", message = "姓名长度2-20个字符，仅支持中文/英文")
    @TableField("real_name")
    @Schema(description = "姓名")
    private String realName;

    /**
     * 电话
     */
    @NotBlank(message = "电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    @TableField("tel")
    @Schema(description = "手机号")
    private String tel;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Pattern(regexp = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$", message = "邮箱格式错误")
    @TableField("email")
    @Schema(description = "邮箱")
    private String email;

    /**
     * 创建时间
     */
    @TableField("input_time")
    @Schema(description = "注册时间")
    private LocalDate inputTime;

    /**
     * 状态：0正常，1锁定，2注销
     */
    @TableField("state")
    @Schema(description = "状态：0正常，1锁定，2注销")
    private Integer state;

    /**
     * 部门id
     */
    @TableField("dept_id")
    @Schema(description = "部门ID")
    private Long deptId;

    /**
     * 员工类型 ， 1平台普通员工 ，2平台客服人员，3平台管理员，4机构员工，5机构管理员（修正为Integer，原Boolean错误）
     */
    @TableField("type")
    @Schema(description = "员工类型：1平台普通员工，2平台客服，3平台管理员，4机构员工，5机构管理员")
    private Integer type;

    @TableField("login_id")
    @Schema(description = "登录ID")
    private Long loginId;

    // 自动生成创建时间（新增时）
    @TableField(exist = false) // 非数据库字段
    private transient LocalDate createTime;
}