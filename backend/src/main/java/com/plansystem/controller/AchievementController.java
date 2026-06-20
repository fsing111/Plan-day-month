package com.plansystem.controller;

import com.plansystem.dto.PageResult;
import com.plansystem.dto.Result;
import com.plansystem.service.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "成果管理")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

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
}
