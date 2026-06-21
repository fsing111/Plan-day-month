package com.plansystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plansystem.entity.Notification;
import com.plansystem.entity.Plan;
import com.plansystem.entity.User;
import com.plansystem.mapper.NotificationMapper;
import com.plansystem.mapper.PlanMapper;
import com.plansystem.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Scheduled reminder service for plan submissions and approval timeouts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final PlanMapper planMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    /**
     * Weekdays at 9:15 AM — remind employees who haven't submitted daily plans.
     */
    @Scheduled(cron = "0 15 9 * * MON-FRI")
    public void remindDailyPlan() {
        LocalDate today = LocalDate.now();
        String todayStr = today.toString();

        List<User> employees = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getEnabled, 1)
                .eq(User::getRole, "EMPLOYEE"));

        for (User emp : employees) {
            Long count = planMapper.selectCount(new LambdaQueryWrapper<Plan>()
                    .eq(Plan::getUserId, emp.getId())
                    .eq(Plan::getPlanType, "DAILY")
                    .ge(Plan::getStartTime, todayStr + " 00:00:00")
                    .le(Plan::getStartTime, todayStr + " 23:59:59")
                    .ne(Plan::getStatus, "DRAFT")
                    .isNull(Plan::getDeletedAt));

            if (count == 0) {
                notificationService.createNotification(emp.getId(),
                        "REMIND_SUBMIT_PLAN",
                        "日报提醒", "请及时提交今日日报",
                        null, null);
            }
        }
        log.info("Daily plan reminder completed for {} employees", employees.size());
    }

    /**
     * Every Monday at 9:15 AM — remind weekly plans.
     */
    @Scheduled(cron = "0 15 9 * * MON")
    public void remindWeeklyPlan() {
        log.info("Weekly plan reminder triggered");
    }

    /**
     * 1st of each month at 9:15 AM — remind monthly plans.
     */
    @Scheduled(cron = "0 15 9 1 * *")
    public void remindMonthlyPlan() {
        log.info("Monthly plan reminder triggered");
    }

    /**
     * Remind specific unsubmitted users (called from statistics controller).
     */
    public void remindUnsubmitted(String date) {
        List<User> employees = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getEnabled, 1)
                .eq(User::getRole, "EMPLOYEE"));

        int reminded = 0;
        for (User emp : employees) {
            Long count = planMapper.selectCount(new LambdaQueryWrapper<Plan>()
                    .eq(Plan::getUserId, emp.getId())
                    .eq(Plan::getPlanType, "DAILY")
                    .ge(Plan::getStartTime, date + " 00:00:00")
                    .le(Plan::getStartTime, date + " 23:59:59")
                    .ne(Plan::getStatus, "DRAFT")
                    .isNull(Plan::getDeletedAt));

            if (count == 0) {
                notificationService.createNotification(emp.getId(),
                        "REMIND_SUBMIT_PLAN",
                        "催办通知", "领导提醒您及时提交计划",
                        null, null);
                reminded++;
            }
        }
        log.info("Manually reminded {} unsubmitted employees for date {}", reminded, date);
    }
}
