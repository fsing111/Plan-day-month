package com.plansystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plansystem.entity.Plan;
import com.plansystem.mapper.PlanMapper;
import com.plansystem.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final PlanMapper planMapper;
    private final UserService userService;

    public Map<String, Object> getPersonalStats() {
        Long userId = UserContext.getUserId();
        List<Plan> plans = planMapper.selectList(new LambdaQueryWrapper<Plan>().eq(Plan::getUserId, userId));

        long total = plans.size();
        long approved = plans.stream().filter(p -> "APPROVED".equals(p.getStatus())).count();
        long rejected = plans.stream().filter(p -> "REJECTED".equals(p.getStatus())).count();

        Map<String, Object> byType = new LinkedHashMap<>();
        for (String type : List.of("DAILY", "WEEKLY", "MONTHLY")) {
            long t = plans.stream().filter(p -> type.equals(p.getPlanType())).count();
            long a = plans.stream().filter(p -> type.equals(p.getPlanType()) && "APPROVED".equals(p.getStatus())).count();
            byType.put(type, Map.of("total", t, "approved", a, "rate", t > 0 ? Math.round(a * 1000.0 / t) / 10.0 : 0));
        }

        return Map.of("summary", Map.of("totalPlans", total, "approvedPlans", approved,
                "rejectedPlans", rejected, "completionRate", total > 0 ? Math.round(approved * 1000.0 / total) / 10.0 : 0),
                "byType", byType);
    }

    public Map<String, Object> getTeamStats() {
        Long userId = UserContext.getUserId();
        List<Long> subordinateIds = userService.getAllSubordinateIds(userId);
        if (subordinateIds.isEmpty()) return Map.of("teamSummary", Map.of("totalMembers", 0));

        List<Plan> plans = planMapper.selectList(new LambdaQueryWrapper<Plan>().in(Plan::getUserId, subordinateIds));
        long total = plans.size();
        long approved = plans.stream().filter(p -> "APPROVED".equals(p.getStatus())).count();

        return Map.of("teamSummary", Map.of("totalMembers", subordinateIds.size(),
                "totalPlans", total, "approvedPlans", approved,
                "completionRate", total > 0 ? Math.round(approved * 1000.0 / total) / 10.0 : 0));
    }
}
