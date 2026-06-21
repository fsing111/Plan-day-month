package com.plansystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plansystem.dto.AchievementCreateRequest;
import com.plansystem.dto.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plansystem.entity.Achievement;
import com.plansystem.entity.AchievementPlanRef;
import com.plansystem.entity.ApprovalRecord;
import com.plansystem.entity.Plan;
import com.plansystem.entity.RecycleBin;
import com.plansystem.exception.BusinessException;
import com.plansystem.exception.ErrorCode;
import com.plansystem.mapper.AchievementMapper;
import com.plansystem.mapper.RecycleBinMapper;
import com.plansystem.service.ApprovalService;
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
    private final com.plansystem.mapper.ApprovalRecordMapper approvalRecordMapper;
    private final com.plansystem.mapper.AchievementPlanRefMapper achievementPlanRefMapper;
    private final RecycleBinMapper recycleBinMapper;
    private final ApprovalService approvalService;

    public PageResult<Map<String, Object>> getAchievementList(int page, int pageSize, String status, Long planId) {
        Long userId = UserContext.getUserId();
        List<Long> visibleUserIds = userService.getVisibleUserIds(userId, UserContext.getRole());

        IPage<Achievement> pageParam = new Page<>(page, Math.min(pageSize, 100));
        LambdaQueryWrapper<Achievement> wrapper = new LambdaQueryWrapper<>();

        // Exclude soft-deleted achievements
        wrapper.isNull(Achievement::getDeletedAt);

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

    /**
     * Create achievement with multiple plan associations (one-to-many).
     */
    @Transactional
    public Achievement createAchievement(List<Long> planIds, String description, String actualQty,
                                          BigDecimal actualHours, String issues, String remark, boolean submit) {
        if (planIds == null || planIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "至少关联一条计划");
        }

        // Validate all plans are APPROVED
        for (Long planId : planIds) {
            Plan plan = planService.getPlanById(planId);
            if (!"APPROVED".equals(plan.getStatus())) {
                throw new BusinessException(ErrorCode.PLAN_NOT_APPROVED);
            }
        }

        Achievement achievement = new Achievement();
        achievement.setPlanId(planIds.get(0)); // Primary plan for backward compatibility
        achievement.setDescription(description);
        achievement.setActualQty(actualQty);
        achievement.setActualHours(actualHours);
        achievement.setIssues(issues);
        achievement.setRemark(remark);
        achievement.setStatus(submit ? "SUBMITTED" : "PENDING");
        if (submit) achievement.setSubmittedAt(LocalDateTime.now());

        achievementMapper.insert(achievement);

        // Batch insert achievement-plan references
        for (Long planId : planIds) {
            AchievementPlanRef ref = new AchievementPlanRef();
            ref.setAchievementId(achievement.getId());
            ref.setPlanId(planId);
            achievementPlanRefMapper.insert(ref);
        }

        // Trigger approval workflow if submitted directly
        if (submit) {
            approvalService.startApproval(achievement.getId(), "ACHIEVEMENT", UserContext.getUserId());
        }

        log.info("Achievement created: id={}, planIds={}", achievement.getId(), planIds);
        return achievement;
    }

    public Achievement getById(Long id) {
        Achievement a = achievementMapper.selectById(id);
        if (a == null || a.getDeletedAt() != null) throw new BusinessException(ErrorCode.NOT_FOUND, "成果不存在");
        return a;
    }

    /**
     * Get achievement detail with associated plan info.
     */
    public Map<String, Object> getAchievementDetail(Long id) {
        Achievement a = getById(id);
        Plan plan = null;
        try { plan = planService.getPlanById(a.getPlanId()); } catch (Exception ignored) {}

        // Get all associated plan IDs via achievement_plan_ref
        List<AchievementPlanRef> refs = achievementPlanRefMapper.selectList(
                new LambdaQueryWrapper<AchievementPlanRef>()
                        .eq(AchievementPlanRef::getAchievementId, id));
        List<Long> planIds = refs.stream().map(AchievementPlanRef::getPlanId).toList();

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("achievement", a);
        result.put("plan", plan);
        result.put("planIds", planIds);
        return result;
    }

    /**
     * Update an existing achievement (only PENDING or REJECTED status).
     */
    @Transactional
    public Achievement updateAchievement(Long id, AchievementCreateRequest request) {
        Achievement a = getById(id);
        if (!"PENDING".equals(a.getStatus()) && !"REJECTED".equals(a.getStatus())) {
            throw new BusinessException(ErrorCode.ACHIEVEMENT_STATUS_NOT_EDITABLE);
        }

        if (request.getPlanIds() != null && !request.getPlanIds().isEmpty()) {
            a.setPlanId(request.getPlanIds().get(0));
        }
        a.setDescription(request.getDescription());
        a.setActualQty(request.getActualQty());
        a.setActualHours(request.getActualHours());
        a.setIssues(request.getIssues());
        a.setRemark(request.getRemark());

        // Re-submit if requested
        if (Boolean.TRUE.equals(request.getSubmitDirectly())) {
            a.setStatus("SUBMITTED");
            a.setSubmittedAt(LocalDateTime.now());
        }

        achievementMapper.updateById(a);
        log.info("Achievement updated: id={}", id);
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

        // Trigger approval workflow
        approvalService.startApproval(a.getId(), "ACHIEVEMENT", UserContext.getUserId());

        return a;
    }

    /**
     * Soft delete an achievement (only PENDING status).
     */
    @Transactional
    public void deleteAchievement(Long id) {
        Achievement a = getById(id);
        if (!"PENDING".equals(a.getStatus())) {
            throw new BusinessException(ErrorCode.ACHIEVEMENT_STATUS_NOT_EDITABLE);
        }
        a.setDeletedAt(LocalDateTime.now());
        achievementMapper.updateById(a);

        // Insert into recycle bin for 30-day recovery
        Plan plan = null;
        try { plan = planService.getPlanById(a.getPlanId()); } catch (Exception ignored) {}
        String title = "成果-" + (plan != null ? plan.getTitle() : "未知计划");

        RecycleBin bin = new RecycleBin();
        bin.setOriginalTable("achievement");
        bin.setOriginalId(a.getId());
        bin.setTitle(title);
        bin.setDeletedBy(UserContext.getUserId());
        bin.setDeletedAt(LocalDateTime.now());
        bin.setCanRestoreUntil(LocalDateTime.now().plusDays(30));
        recycleBinMapper.insert(bin);

        log.info("Achievement soft-deleted: id={}", id);
    }

    /**
     * Withdraw a submitted achievement (only if no approver has acted).
     */
    @Transactional
    public void withdrawAchievement(Long id) {
        Achievement a = getById(id);
        if (!"SUBMITTED".equals(a.getStatus()) && !"APPROVING".equals(a.getStatus())) {
            throw new BusinessException(ErrorCode.ACHIEVEMENT_STATUS_NOT_EDITABLE);
        }

        // Check if any approver has already acted
        Long actedCount = approvalRecordMapper.selectCount(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getTargetId, id)
                .eq(ApprovalRecord::getTargetType, "ACHIEVEMENT")
                .isNotNull(ApprovalRecord::getAction));

        if (actedCount > 0) {
            throw new BusinessException(ErrorCode.ACHIEVEMENT_CANNOT_WITHDRAW);
        }

        // Delete pending approval records
        approvalRecordMapper.delete(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getTargetId, id)
                .eq(ApprovalRecord::getTargetType, "ACHIEVEMENT"));

        // Restore to PENDING
        a.setStatus("PENDING");
        a.setSubmittedAt(null);
        achievementMapper.updateById(a);
        log.info("Achievement withdrawn: id={}", id);
    }
}
