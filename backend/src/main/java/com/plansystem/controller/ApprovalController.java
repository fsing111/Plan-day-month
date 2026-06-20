package com.plansystem.controller;

import com.plansystem.dto.Result;
import com.plansystem.entity.ApprovalRecord;
import com.plansystem.service.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "审批管理")
@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @Operation(summary = "待审批列表")
    @GetMapping("/pending")
    public Result<List<ApprovalRecord>> pending() {
        return Result.success(approvalService.getPendingApprovals());
    }

    @Operation(summary = "通过审批")
    @PostMapping("/{recordId}/approve")
    public Result<Void> approve(@PathVariable Long recordId, @RequestBody Map<String, String> body) {
        approvalService.approve(recordId, body.get("comment"));
        return Result.success("审批通过", null);
    }

    @Operation(summary = "驳回审批")
    @PostMapping("/{recordId}/reject")
    public Result<Void> reject(@PathVariable Long recordId, @RequestBody Map<String, String> body) {
        approvalService.reject(recordId, body.get("comment"));
        return Result.success("已驳回", null);
    }

    @Operation(summary = "转审")
    @PostMapping("/{recordId}/transfer")
    public Result<Void> transfer(@PathVariable Long recordId, @RequestBody Map<String, Object> body) {
        Long targetUserId = Long.valueOf(body.get("targetUserId").toString());
        String comment = (String) body.get("comment");
        approvalService.transfer(recordId, targetUserId, comment);
        return Result.success("已转审", null);
    }
}
