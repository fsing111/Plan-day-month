package com.plansystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plansystem.dto.PageResult;
import com.plansystem.entity.Achievement;
import com.plansystem.entity.Plan;
import com.plansystem.entity.RecycleBin;
import com.plansystem.exception.BusinessException;
import com.plansystem.exception.ErrorCode;
import com.plansystem.mapper.AchievementMapper;
import com.plansystem.mapper.PlanMapper;
import com.plansystem.mapper.RecycleBinMapper;
import com.plansystem.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Recycle bin service — soft-delete recovery and permanent cleanup.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecycleBinService {

    private final RecycleBinMapper recycleBinMapper;
    private final PlanMapper planMapper;
    private final AchievementMapper achievementMapper;

    public PageResult<RecycleBin> getDeletedItems(int page, int pageSize) {
        IPage<RecycleBin> pageParam = new Page<>(page, Math.min(pageSize, 100));
        IPage<RecycleBin> result = recycleBinMapper.selectPage(pageParam,
                new LambdaQueryWrapper<RecycleBin>()
                        .eq(RecycleBin::getDeletedBy, UserContext.getUserId())
                        .orderByDesc(RecycleBin::getDeletedAt));
        return PageResult.of(result);
    }

    @Transactional
    public void restoreItem(Long id) {
        RecycleBin item = recycleBinMapper.selectById(id);
        if (item == null) throw new BusinessException(ErrorCode.NOT_FOUND, "回收项不存在");

        if (item.getCanRestoreUntil() != null && LocalDateTime.now().isAfter(item.getCanRestoreUntil())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "已超过30天恢复期限，不可恢复");
        }

        if ("plan".equals(item.getOriginalTable())) {
            Plan plan = planMapper.selectById(item.getOriginalId());
            if (plan != null) {
                plan.setDeletedAt(null);
                planMapper.updateById(plan);
            }
        } else if ("achievement".equals(item.getOriginalTable())) {
            Achievement a = achievementMapper.selectById(item.getOriginalId());
            if (a != null) {
                a.setDeletedAt(null);
                achievementMapper.updateById(a);
            }
        }

        recycleBinMapper.deleteById(id);
        log.info("Restored item: table={}, id={}", item.getOriginalTable(), item.getOriginalId());
    }

    @Transactional
    public void permanentDelete(Long id) {
        RecycleBin item = recycleBinMapper.selectById(id);
        if (item == null) return;

        // Actually delete from original table (use physicalDelete to bypass @TableLogic)
        if ("plan".equals(item.getOriginalTable())) {
            planMapper.physicalDelete(item.getOriginalId());
        } else if ("achievement".equals(item.getOriginalTable())) {
            achievementMapper.physicalDelete(item.getOriginalId());
        }

        recycleBinMapper.deleteById(id);
        log.info("Permanently deleted: table={}, id={}", item.getOriginalTable(), item.getOriginalId());
    }

    /**
     * Daily at 4:00 AM — permanently delete items older than 30 days.
     */
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void cleanExpiredItems() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        recycleBinMapper.delete(new LambdaQueryWrapper<RecycleBin>()
                .lt(RecycleBin::getCanRestoreUntil, threshold));
    }
}
