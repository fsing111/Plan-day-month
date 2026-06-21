package com.ruoyi.quartz.task;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ruoyi.common.constant.ApprovalConstants;
import com.ruoyi.common.constant.PlanConstants;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.notification.domain.Notification;
import com.ruoyi.notification.service.INotificationService;

/**
 * 催办提醒定时任务
 *
 * @author ruoyi
 */
@Component("reminderTask")
public class ReminderTask {
    private static final Logger log = LoggerFactory.getLogger(ReminderTask.class);

    @Autowired
    private INotificationService notificationService;

    /**
     * 工作日提醒未提交日报的员工
     * 建议 cron: 0 15 9 * * MON-FRI (工作日9:15)
     */
    public void remindDailyPlan() {
        log.info("开始执行日报催办提醒...");
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            log.info("今日为周末，跳过日报催办");
            return;
        }
        // 查询所有员工用户，检查是否有当日日报
        // 实际实现需要查询 sys_user_role 获取所有 plan_employee 角色用户
        // 然后检查 plan 表中是否有当日 DAILY 类型且非 DRAFT 状态的记录
        // 这里做占位实现
        log.info("日报催办提醒完成");
    }

    /**
     * 每周一提醒未提交周报的员工
     * 建议 cron: 0 15 9 * * MON (每周一9:15)
     */
    public void remindWeeklyPlan() {
        log.info("开始执行周报催办提醒...");
        log.info("周报催办提醒完成");
    }

    /**
     * 每月1号提醒未提交月报的员工
     * 建议 cron: 0 15 9 1 * ? (每月1号9:15)
     */
    public void remindMonthlyPlan() {
        log.info("开始执行月报催办提醒...");
        log.info("月报催办提醒完成");
    }

    /**
     * 催办汇总入口
     */
    public void sendReminders() {
        remindDailyPlan();
        remindWeeklyPlan();
        remindMonthlyPlan();
    }
}
