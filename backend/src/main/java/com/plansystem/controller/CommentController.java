package com.plansystem.controller;

import com.plansystem.dto.Result;
import com.plansystem.entity.PlanComment;
import com.plansystem.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "评论讨论")
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "获取评论列表")
    @GetMapping
    public Result<List<PlanComment>> list(@RequestParam Long targetId,
                                           @RequestParam String targetType) {
        return Result.success(commentService.getComments(targetId, targetType));
    }

    @Operation(summary = "添加评论")
    @PostMapping
    public Result<PlanComment> add(@RequestBody Map<String, String> body) {
        Long targetId = Long.valueOf(body.get("targetId"));
        String targetType = body.get("targetType");
        String content = body.get("content");
        return Result.success(commentService.addComment(targetId, targetType, content));
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success("删除成功", null);
    }
}
