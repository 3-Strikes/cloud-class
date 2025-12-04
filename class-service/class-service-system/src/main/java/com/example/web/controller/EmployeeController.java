package com.example.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.domain.Employee;
import com.example.query.EmployeeQuery;
import com.example.result.JSONResult;
import com.example.result.PageList;
import com.example.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * 员工控制器
 *
 * @author lzy
 * @since 2025-12-04
 */
@RestController
@RequestMapping("/employee")
@Tag(name = "员工管理", description = "员工增删改查接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 保存/修改员工（新增/编辑共用）
     */
    @PostMapping("/save")
    @Operation(summary = "新增/编辑员工", description = "ID不为空则编辑，为空则新增")
    public JSONResult saveOrUpdate(@Valid @RequestBody Employee employee) {
        try {
            // 新增时自动填充创建时间
            if (employee.getId() == null) {
                employee.setInputTime(LocalDate.now());
            }
            boolean success = employeeService.saveOrUpdate(employee);
            return success ? JSONResult.success() : JSONResult.error("操作失败");
        } catch (Exception e) {
            return JSONResult.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "单个删除员工", description = "根据ID删除员工")
    public JSONResult delete(@PathVariable("id") Long id) {
        try {
            boolean success = employeeService.removeById(id);
            return success ? JSONResult.success() : JSONResult.error("删除失败");
        } catch (Exception e) {
            return JSONResult.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/batch/{ids}")
    @Operation(summary = "批量删除员工", description = "ID以逗号分隔，如：1,2,3")
    public JSONResult batchDelete(@PathVariable("ids") String ids) {
        try {
            List<Long> idList = Arrays.stream(ids.split(","))
                    .map(Long::parseLong)
                    .toList();
            boolean success = employeeService.removeByIds(idList);
            return success ? JSONResult.success() : JSONResult.error("批量删除失败");
        } catch (Exception e) {
            return JSONResult.error("批量删除失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询员工
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询单个员工", description = "根据ID查询员工详情")
    public JSONResult get(@PathVariable("id") Long id) {
        return JSONResult.success(employeeService.getById(id));
    }

    /**
     * 查询所有员工
     */
    @GetMapping("/list")
    @Operation(summary = "查询所有员工", description = "无分页，返回所有员工")
    public JSONResult list() {
        return JSONResult.success(employeeService.list());
    }

    /**
     * 带条件分页查询（核心优化：处理keyword模糊查询）
     */
    @PostMapping("/pagelist")
    @Operation(summary = "分页查询员工", description = "支持姓名/电话/邮箱模糊查询，分页返回")
    public JSONResult page(@RequestBody EmployeeQuery query) {
        try {
            // 构建分页对象
            Page<Employee> page = new Page<>(query.getPage(), query.getRows());

            // 构建条件查询器
            LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
            // 关键字模糊查询（姓名/电话/邮箱）
            if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
                wrapper.like(Employee::getRealName, query.getKeyword().trim())
                        .or()
                        .like(Employee::getTel, query.getKeyword().trim())
                        .or()
                        .like(Employee::getEmail, query.getKeyword().trim());
            }
            // 状态筛选（可选）
            if (query.getState() != null) {
                wrapper.eq(Employee::getState, query.getState());
            }
            // 按创建时间降序排序
            wrapper.orderByDesc(Employee::getInputTime);

            // 执行分页查询
            Page<Employee> resultPage = employeeService.page(page, wrapper);

            return JSONResult.success(new PageList<>(resultPage.getTotal(), resultPage.getRecords()));
        } catch (Exception e) {
            return JSONResult.error("分页查询失败：" + e.getMessage());
        }
    }
}