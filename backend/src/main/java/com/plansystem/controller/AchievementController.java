package com.plansystem.controller;

import com.plansystem.dto.AchievementCreateRequest;
import com.plansystem.dto.PageResult;
import com.plansystem.dto.Result;
import com.plansystem.entity.Achievement;
import com.plansystem.service.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Tag(name = "成果管理")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @Operation(summary = "创建成果（支持关联多条计划）")
    @PostMapping("/achievements")
    public Result<Achievement> create(@Valid @RequestBody AchievementCreateRequest request) {
        Achievement a = achievementService.createAchievement(
                request.getPlanIds(),
                request.getDescription(),
                request.getActualQty(),
                request.getActualHours(),
                request.getIssues(),
                request.getRemark(),
                Boolean.TRUE.equals(request.getSubmitDirectly()));
        return Result.success("创建成功", a);
    }

    @Operation(summary = "查看成果详情")
    @GetMapping("/achievements/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(achievementService.getAchievementDetail(id));
    }

    @Operation(summary = "修改成果")
    @PutMapping("/achievements/{id}")
    public Result<Achievement> update(@PathVariable Long id, @Valid @RequestBody AchievementCreateRequest request) {
        Achievement a = achievementService.updateAchievement(id, request);
        return Result.success("修改成功", a);
    }

    @Operation(summary = "查询成果列表")
    @GetMapping("/achievements")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long planId) {
        return Result.success(achievementService.getAchievementList(page, pageSize, status, planId));
    }

    @Operation(summary = "提交成果")
    @PostMapping("/achievements/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        achievementService.submitAchievement(id);
        return Result.success("提交成功", null);
    }

    @Operation(summary = "撤回成果（提交后审批前撤回至待填写）")
    @PostMapping("/achievements/{id}/withdraw")
    public Result<Void> withdraw(@PathVariable Long id) {
        achievementService.withdrawAchievement(id);
        return Result.success("撤回成功", null);
    }

    @Operation(summary = "删除成果（软删除）")
    @DeleteMapping("/achievements/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        achievementService.deleteAchievement(id);
        return Result.success("删除成功", null);
    }
}
