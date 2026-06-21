package com.plansystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plansystem.entity.Plan;
import com.plansystem.mapper.PlanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled task: mark overdue plans (APPROVED past end_time, no achievement).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanOverdueService {

    private final PlanMapper planMapper;

    /**
     * Daily at 1:00 AM — mark overdue plans.
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void scanAndMarkOverdue() {
        List<Plan> plans = planMapper.selectList(new LambdaQueryWrapper<Plan>()
                .eq(Plan::getStatus, "APPROVED")
                .lt(Plan::getEndTime, LocalDateTime.now())
                .isNull(Plan::getDeletedAt));

        int count = 0;
        for (Plan p : plans) {
            p.setStatus("OVERDUE");
            planMapper.updateById(p);
            count++;
        }

        if (count > 0) {
            log.info("Marked {} plans as overdue", count);
        }
    }
}
