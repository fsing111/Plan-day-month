package com.plansystem.controller;

import com.plansystem.dto.PageResult;
import com.plansystem.dto.Result;
import com.plansystem.entity.Notification;
import com.plansystem.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "通知管理")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "通知列表")
    @GetMapping
    public Result<PageResult<Notification>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(notificationService.getNotifications(page, pageSize));
    }

    @Operation(summary = "未读数量")
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount() {
        return Result.success(Map.of("count", notificationService.getUnreadCount()));
    }

    @Operation(summary = "标记已读")
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success(null);
    }

    @Operation(summary = "全部已读")
    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        notificationService.markAllAsRead();
        return Result.success(null);
    }

    @Operation(summary = "删除通知")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return Result.success(null);
    }
}
