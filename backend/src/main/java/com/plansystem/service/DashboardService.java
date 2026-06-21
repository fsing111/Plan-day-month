package com.plansystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plansystem.entity.Notification;
import com.plansystem.entity.Plan;
import com.plansystem.mapper.NotificationMapper;
import com.plansystem.mapper.PlanMapper;
import com.plansystem.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * Dashboard service — role-specific data aggregation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PlanMapper planMapper;
    private final NotificationMapper notificationMapper;
    private final UserService userService;

    public Map<String, Object> getDashboard() {
        Long userId = UserContext.getUserId();
        boolean isLeader = UserContext.isLeader();

        Map<String, Object> data = new HashMap<>();
        data.put("todayPlanCount", getTodayPlanCount(userId));
        data.put("pendingApprovalCount", getPendingApprovalCount(userId));
        data.put("rejectedCount", getRejectedCount(userId));
        data.put("weeklyCompletionRate", getWeeklyCompletionRate(userId));
        data.put("recentNotifications", getRecentNotifications(userId));

        if (isLeader) {
            List<Long> subordinateIds = userService.getAllSubordinateIds(userId);
            data.put("teamTotalMembers", subordinateIds.size());
            data.put("teamSubmittedToday", getTeamSubmittedToday(subordinateIds));
            data.put("unsubmittedMembers", getUnsubmittedMembers(subordinateIds));
            data.put("overdueCount", getOverdueCount(userId));
        }

        return data;
    }

    private long getTodayPlanCount(Long userId) {
        String today = LocalDate.now().toString();
        return planMapper.selectCount(new LambdaQueryWrapper<Plan>()
                .eq(Plan::getUserId, userId)
                .ge(Plan::getStartTime, today + " 00:00:00")
                .le(Plan::getStartTime, today + " 23:59:59")
                .isNull(Plan::getDeletedAt));
    }

    private long getPendingApprovalCount(Long userId) {
        return planMapper.selectCount(new LambdaQueryWrapper<Plan>()
                .eq(Plan::getUserId, userId)
                .in(Plan::getStatus, "SUBMITTED", "APPROVING")
                .isNull(Plan::getDeletedAt));
    }

    private long getRejectedCount(Long userId) {
        return planMapper.selectCount(new LambdaQueryWrapper<Plan>()
                .eq(Plan::getUserId, userId)
                .eq(Plan::getStatus, "REJECTED")
                .isNull(Plan::getDeletedAt));
    }

    private double getWeeklyCompletionRate(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(java.time.DayOfWeek.MONDAY);
        String weekStartStr = weekStart.toString();
        String weekEndStr = weekStart.plusDays(6).toString();

        long total = planMapper.selectCount(new LambdaQueryWrapper<Plan>()
                .eq(Plan::getUserId, userId)
                .ge(Plan::getStartTime, weekStartStr + " 00:00:00")
                .le(Plan::getStartTime, weekEndStr + " 23:59:59")
                .ne(Plan::getStatus, "DRAFT")
                .isNull(Plan::getDeletedAt));

        long completed = planMapper.selectCount(new LambdaQueryWrapper<Plan>()
                .eq(Plan::getUserId, userId)
                .eq(Plan::getStatus, "APPROVED")
                .ge(Plan::getStartTime, weekStartStr + " 00:00:00")
                .le(Plan::getStartTime, weekEndStr + " 23:59:59")
                .isNull(Plan::getDeletedAt));

        return total > 0 ? Math.round(completed * 1000.0 / total) / 10.0 : 0;
    }

    private List<Notification> getRecentNotifications(Long userId) {
        return notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverId, userId)
                .orderByDesc(Notification::getCreatedAt)
                .last("LIMIT 5"));
    }

    private long getTeamSubmittedToday(List<Long> subordinateIds) {
        if (subordinateIds.isEmpty()) return 0;
        String today = LocalDate.now().toString();
        return planMapper.selectCount(new LambdaQueryWrapper<Plan>()
                .in(Plan::getUserId, subordinateIds)
                .ge(Plan::getStartTime, today + " 00:00:00")
                .le(Plan::getStartTime, today + " 23:59:59")
                .ne(Plan::getStatus, "DRAFT")
                .isNull(Plan::getDeletedAt));
    }

    private List<Map<String, Object>> getUnsubmittedMembers(List<Long> subordinateIds) {
        List<Map<String, Object>> result = new ArrayList<>();
        String today = LocalDate.now().toString();
        for (Long id : subordinateIds) {
            long count = planMapper.selectCount(new LambdaQueryWrapper<Plan>()
                    .eq(Plan::getUserId, id)
                    .ge(Plan::getStartTime, today + " 00:00:00")
                    .le(Plan::getStartTime, today + " 23:59:59")
                    .ne(Plan::getStatus, "DRAFT")
                    .isNull(Plan::getDeletedAt));
            if (count == 0) {
                result.add(Map.of("userId", id));
            }
        }
        return result;
    }

    private long getOverdueCount(Long userId) {
        List<Long> ids = userService.getAllSubordinateIds(userId);
        ids.add(userId);
        return planMapper.selectCount(new LambdaQueryWrapper<Plan>()
                .in(Plan::getUserId, ids)
                .eq(Plan::getStatus, "OVERDUE")
                .isNull(Plan::getDeletedAt));
    }
}
