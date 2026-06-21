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
 * Scheduled task: auto-archive plans whose end_time has passed.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanArchiveService {

    private final PlanMapper planMapper;

    /**
     * Daily at 2:00 AM — archive approved plans past their end time.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void scanAndArchive() {
        List<Plan> plans = planMapper.selectList(new LambdaQueryWrapper<Plan>()
                .eq(Plan::getStatus, "APPROVED")
                .lt(Plan::getEndTime, LocalDateTime.now())
                .isNull(Plan::getDeletedAt));

        int count = 0;
        for (Plan p : plans) {
            p.setStatus("ARCHIVED");
            planMapper.updateById(p);
            count++;
        }

        if (count > 0) {
            log.info("Auto-archived {} plans", count);
        }
    }
}
