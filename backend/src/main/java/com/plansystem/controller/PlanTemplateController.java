package com.plansystem.controller;

import com.plansystem.dto.Result;
import com.plansystem.entity.PlanTemplate;
import com.plansystem.service.PlanTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "计划模板")
@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class PlanTemplateController {

    private final PlanTemplateService templateService;

    @Operation(summary = "获取我的模板列表")
    @GetMapping
    public Result<List<PlanTemplate>> list() {
        return Result.success(templateService.getUserTemplates());
    }

    @Operation(summary = "保存为模板")
    @PostMapping
    public Result<PlanTemplate> create(@RequestBody PlanTemplate template) {
        return Result.success(templateService.createTemplate(template));
    }

    @Operation(summary = "删除模板")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return Result.success("删除成功", null);
    }
}
