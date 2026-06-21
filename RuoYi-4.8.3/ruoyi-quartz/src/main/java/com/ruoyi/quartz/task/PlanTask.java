package com.ruoyi.quartz.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ruoyi.plan.mapper.PlanMapper;

/**
 * 计划系统定时任务
 * - 自动归档已通过且到期的计划
 * - 自动标记逾期计划
 *
 * @author ruoyi
 */
@Component("planTask")
public class PlanTask {
    private static final Logger log = LoggerFactory.getLogger(PlanTask.class);

    @Autowired
    private PlanMapper planMapper;

    /**
     * 自动归档：将 end_time 已过且状态为 APPROVED 的计划标记为 ARCHIVED
     * 建议 cron: 0 0 2 * * ? (每日凌晨2:00)
     */
    public void archivePlans() {
        log.info("开始执行计划自动归档任务...");
        int count = planMapper.batchArchivePlans();
        log.info("计划自动归档完成，归档 {} 条", count);
    }

    /**
     * 逾期标记：将 end_time 已过且无成果关联的 APPROVED 计划标记为 OVERDUE
     * 建议 cron: 0 0 3 * * ? (每日凌晨3:00)
     */
    public void markOverdue() {
        log.info("开始执行计划逾期标记任务...");
        int count = planMapper.batchMarkOverdue();
        log.info("计划逾期标记完成，标记 {} 条", count);
    }

    /**
     * 无参方法，用于 Quartz 直接调用
     */
    public void planArchive() {
        archivePlans();
    }

    public void planOverdue() {
        markOverdue();
    }
}
