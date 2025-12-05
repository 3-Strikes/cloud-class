package com.example.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 员工分页查询参数
 *
 * @author lzy
 * @since 2025-12-04
 */
@Data
@Schema(description = "员工分页查询参数")
public class EmployeeQuery extends BaseQuery {

    @Schema(description = "关键字（姓名/电话/邮箱模糊查询）")
    private String keyword;

    @Schema(description = "状态：0正常，1锁定，2注销（可选）")
    private Integer state;
}