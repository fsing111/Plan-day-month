package com.plansystem.controller;

import com.plansystem.dto.PageResult;
import com.plansystem.dto.Result;
import com.plansystem.entity.RecycleBin;
import com.plansystem.service.RecycleBinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "回收站")
@RestController
@RequestMapping("/api/v1/recycle-bin")
@RequiredArgsConstructor
public class RecycleBinController {

    private final RecycleBinService recycleBinService;

    @Operation(summary = "回收站列表")
    @GetMapping
    public Result<PageResult<RecycleBin>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(recycleBinService.getDeletedItems(page, pageSize));
    }

    @Operation(summary = "恢复删除项")
    @PostMapping("/{id}/restore")
    public Result<Void> restore(@PathVariable Long id) {
        recycleBinService.restoreItem(id);
        return Result.success("恢复成功", null);
    }

    @Operation(summary = "彻底删除")
    @DeleteMapping("/{id}")
    public Result<Void> permanentDelete(@PathVariable Long id) {
        recycleBinService.permanentDelete(id);
        return Result.success("已彻底删除", null);
    }
}
