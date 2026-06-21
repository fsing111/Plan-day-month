package com.plansystem.controller;

import com.plansystem.dto.Result;
import com.plansystem.service.ReminderService;
import com.plansystem.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "统计分析")
@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;
    private final ReminderService reminderService;

    @Operation(summary = "个人统计")
    @GetMapping("/personal")
    public Result<Map<String, Object>> personal() {
        return Result.success(statisticsService.getPersonalStats());
    }

    @Operation(summary = "团队统计")
    @GetMapping("/team")
    public Result<Map<String, Object>> team() {
        return Result.success(statisticsService.getTeamStats());
    }

    @Operation(summary = "趋势数据")
    @GetMapping("/trend")
    public Result<Object> trend() {
        return Result.success("趋势数据功能实现中");
    }

    @Operation(summary = "一键催办未提交人员")
    @PostMapping("/remind")
    public Result<Void> remindUnsubmitted(@RequestBody Map<String, String> body) {
        String date = body.getOrDefault("date", java.time.LocalDate.now().toString());
        reminderService.remindUnsubmitted(date);
        return Result.success("催办通知已发送", null);
    }
}
