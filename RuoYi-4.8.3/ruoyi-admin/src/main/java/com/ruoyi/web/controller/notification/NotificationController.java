package com.ruoyi.web.controller.notification;

import java.util.List;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.notification.service.INotificationService;

@Controller
@RequestMapping("/notification/notification")
public class NotificationController extends BaseController {
    private String prefix = "notification/notification";

    @Autowired
    private INotificationService notificationService;

    @RequiresPermissions("notification:notification:view")
    @GetMapping()
    public String notification() { return prefix + "/notification"; }

    @RequiresPermissions("notification:notification:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list() {
        startPage();
        List<?> list = notificationService.selectNotificationList(ShiroUtils.getUserId());
        return getDataTable(list);
    }

    @GetMapping("/unread-count")
    @ResponseBody
    public AjaxResult unreadCount() {
        int count = notificationService.getUnreadCount(ShiroUtils.getUserId());
        return AjaxResult.success(count);
    }

    @RequiresPermissions("notification:notification:read")
    @PostMapping("/read/{id}")
    @ResponseBody
    public AjaxResult markRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return success();
    }

    @PostMapping("/read-all")
    @ResponseBody
    public AjaxResult markAllRead() {
        notificationService.markAllAsRead(ShiroUtils.getUserId());
        return success();
    }

    @RequiresPermissions("notification:notification:remove")
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        for (String id : ids.split(",")) {
            notificationService.deleteById(Long.valueOf(id));
        }
        return success();
    }
}
