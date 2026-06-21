package com.plansystem.controller;

import com.plansystem.dto.PageResult;
import com.plansystem.dto.Result;
import com.plansystem.entity.*;
import com.plansystem.mapper.ProjectCategoryMapper;
import com.plansystem.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "系统管理")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final ProjectCategoryMapper categoryMapper;

    @Operation(summary = "用户列表")
    @PreAuthorize("hasRole('LEADER')")
    @GetMapping("/users")
    public Result<PageResult<User>> users(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize, @RequestParam(required = false) String keyword) {
        return Result.success(adminService.getUserList(page, pageSize, keyword));
    }

    @Operation(summary = "添加用户")
    @PreAuthorize("hasRole('LEADER')")
    @PostMapping("/users")
    public Result<Void> createUser(@RequestBody User user) {
        adminService.createUser(user);
        return Result.success("创建成功", null);
    }

    @Operation(summary = "编辑用户")
    @PreAuthorize("hasRole('LEADER')")
    @PutMapping("/users/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody User user) {
        adminService.updateUser(id, user);
        return Result.success("修改成功", null);
    }

    @Operation(summary = "禁用/启用")
    @PreAuthorize("hasRole('LEADER')")
    @PutMapping("/users/{id}/disable")
    public Result<Void> toggleUser(@PathVariable Long id) {
        adminService.toggleUser(id);
        return Result.success(null);
    }

    @Operation(summary = "重置密码")
    @PreAuthorize("hasRole('LEADER')")
    @PutMapping("/users/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        adminService.resetPassword(id);
        return Result.success("密码已重置为123456", null);
    }

    @Operation(summary = "部门列表")
    @PreAuthorize("hasRole('LEADER')")
    @GetMapping("/departments")
    public Result<List<Department>> departments() {
        return Result.success(adminService.getDepartments());
    }

    @PreAuthorize("hasRole('LEADER')")
    @PostMapping("/departments") public Result<Void> createDept(@RequestBody Department d) { adminService.createDepartment(d); return Result.success(null); }
    @PreAuthorize("hasRole('LEADER')")
    @PutMapping("/departments/{id}") public Result<Void> updateDept(@PathVariable Long id, @RequestBody Department d) { adminService.updateDepartment(id, d); return Result.success(null); }
    @DeleteMapping("/departments/{id}") public Result<Void> deleteDept(@PathVariable Long id) { adminService.deleteDepartment(id); return Result.success(null); }

    @Operation(summary = "审批链列表")
    @PreAuthorize("hasRole('LEADER')")
    @GetMapping("/approval-chains")
    public Result<List<ApprovalChain>> chains() { return Result.success(adminService.getChains()); }
    @PreAuthorize("hasRole('LEADER')")
    @PostMapping("/approval-chains") public Result<Void> createChain(@RequestBody ApprovalChain c) { adminService.createChain(c); return Result.success(null); }
    @PreAuthorize("hasRole('LEADER')")
    @PutMapping("/approval-chains/{id}") public Result<Void> updateChain(@PathVariable Long id, @RequestBody ApprovalChain c) { adminService.updateChain(id, c); return Result.success(null); }
    @DeleteMapping("/approval-chains/{id}") public Result<Void> deleteChain(@PathVariable Long id) { adminService.deleteChain(id); return Result.success(null); }

    @Operation(summary = "分类列表")
    @PreAuthorize("hasRole('LEADER')")
    @GetMapping("/categories")
    public Result<List<ProjectCategory>> categories() { return Result.success(categoryMapper.selectList(null)); }
    @PreAuthorize("hasRole('LEADER')")
    @PostMapping("/categories") public Result<Void> createCategory(@RequestBody ProjectCategory c) { categoryMapper.insert(c); return Result.success(null); }
    @PreAuthorize("hasRole('LEADER')")
    @PutMapping("/categories/{id}") public Result<Void> updateCategory(@PathVariable Long id, @RequestBody ProjectCategory c) { c.setId(id); categoryMapper.updateById(c); return Result.success(null); }
    @DeleteMapping("/categories/{id}") public Result<Void> deleteCategory(@PathVariable Long id) { categoryMapper.deleteById(id); return Result.success(null); }
}
