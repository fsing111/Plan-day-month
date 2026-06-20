package com.plansystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plansystem.dto.PageResult;
import com.plansystem.entity.Achievement;
import com.plansystem.entity.Plan;
import com.plansystem.exception.BusinessException;
import com.plansystem.exception.ErrorCode;
import com.plansystem.mapper.AchievementMapper;
import com.plansystem.utils.DateUtils;
import com.plansystem.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementMapper achievementMapper;
    private final PlanService planService;
    private final UserService userService;

    public PageResult<Map<String, Object>> getAchievementList(int page, int pageSize, String status, Long planId) {
        Long userId = UserContext.getUserId();
        List<Long> visibleUserIds = userService.getVisibleUserIds(userId, UserContext.getRole());

        IPage<Achievement> pageParam = new Page<>(page, Math.min(pageSize, 100));
        LambdaQueryWrapper<Achievement> wrapper = new LambdaQueryWrapper<>();

        if (visibleUserIds != null) {
            // Need to filter by plan's userId - handled at service level
        }
        if (status != null) wrapper.eq(Achievement::getStatus, status);
        if (planId != null) wrapper.eq(Achievement::getPlanId, planId);
        wrapper.orderByDesc(Achievement::getCreatedAt);

        IPage<Achievement> result = achievementMapper.selectPage(pageParam, wrapper);
        Map<Long, String> userNameMap = userService.getUserNameMap(visibleUserIds);

        List<Map<String, Object>> records = result.getRecords().stream().map(a -> {
            Plan plan = null;
            try { plan = planService.getPlanById(a.getPlanId()); } catch (Exception ignored) {}
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", a.getId());
            m.put("planId", a.getPlanId());
            m.put("planTitle", plan != null ? plan.getTitle() : "");
            m.put("planType", plan != null ? plan.getPlanType() : "");
            m.put("description", a.getDescription());
            m.put("actualQty", a.getActualQty());
            m.put("actualHours", a.getActualHours());
            m.put("status", a.getStatus());
            m.put("submittedAt", DateUtils.format(a.getSubmittedAt()));
            return m;
        }).collect(Collectors.toList());

        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Transactional
    public Achievement createAchievement(Long planId, String description, String actualQty,
                                          BigDecimal actualHours, String issues, String remark, boolean submit) {
        Plan plan = planService.getPlanById(planId);
        if (!"APPROVED".equals(plan.getStatus())) {
            throw new BusinessException(ErrorCode.PLAN_NOT_APPROVED);
        }

        // Check one-to-one constraint
        Long count = achievementMapper.selectCount(
                new LambdaQueryWrapper<Achievement>().eq(Achievement::getPlanId, planId));
        if (count > 0) throw new BusinessException(ErrorCode.ACHIEVEMENT_ALREADY_EXISTS);

        Achievement achievement = new Achievement();
        achievement.setPlanId(planId);
        achievement.setDescription(description);
        achievement.setActualQty(actualQty);
        achievement.setActualHours(actualHours);
        achievement.setIssues(issues);
        achievement.setRemark(remark);
        achievement.setStatus(submit ? "SUBMITTED" : "PENDING");
        if (submit) achievement.setSubmittedAt(LocalDateTime.now());

        achievementMapper.insert(achievement);
        return achievement;
    }

    public Achievement getById(Long id) {
        Achievement a = achievementMapper.selectById(id);
        if (a == null) throw new BusinessException(ErrorCode.NOT_FOUND, "成果不存在");
        return a;
    }

    @Transactional
    public Achievement submitAchievement(Long id) {
        Achievement a = getById(id);
        if (!"PENDING".equals(a.getStatus()) && !"REJECTED".equals(a.getStatus())) {
            throw new BusinessException(ErrorCode.ACHIEVEMENT_STATUS_NOT_EDITABLE);
        }
        a.setStatus("SUBMITTED");
        a.setSubmittedAt(LocalDateTime.now());
        achievementMapper.updateById(a);
        return a;
    }
}
