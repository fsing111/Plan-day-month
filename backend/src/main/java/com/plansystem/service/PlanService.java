package com.plansystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plansystem.dto.PageResult;
import com.plansystem.dto.PlanCreateRequest;
import com.plansystem.dto.PlanVO;
import com.plansystem.entity.Achievement;
import com.plansystem.entity.AchievementPlanRef;
import com.plansystem.entity.ApprovalRecord;
import com.plansystem.entity.Plan;
import com.plansystem.entity.PlanRevision;
import com.plansystem.entity.RecycleBin;
import com.plansystem.exception.BusinessException;
import com.plansystem.exception.ErrorCode;
import com.plansystem.mapper.AchievementMapper;
import com.plansystem.mapper.AchievementPlanRefMapper;
import com.plansystem.mapper.ApprovalRecordMapper;
import com.plansystem.mapper.PlanMapper;
import com.plansystem.mapper.PlanRevisionMapper;
import com.plansystem.mapper.RecycleBinMapper;
import com.plansystem.service.ApprovalService;
import com.plansystem.utils.DateUtils;
import com.plansystem.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Plan service - core business logic for plan management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanMapper planMapper;
    private final UserService userService;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final PlanRevisionMapper planRevisionMapper;
    private final RecycleBinMapper recycleBinMapper;
    private final ApprovalService approvalService;
    private final AchievementMapper achievementMapper;
    private final AchievementPlanRefMapper achievementPlanRefMapper;

    /**
     * Query plans with pagination and data permission filtering.
     */
    public PageResult<PlanVO> getPlanList(Integer page, Integer pageSize, String planType, String status,
                                           String priority, String startDate, String endDate,
                                           Long categoryId, Long userId, String keyword) {
        Long currentUserId = UserContext.getUserId();
        String currentRole = UserContext.getRole();

        IPage<Plan> pageParam = new Page<>(page != null ? page : 1, pageSize != null ? Math.min(pageSize, 100) : 20);
        LambdaQueryWrapper<Plan> wrapper = new LambdaQueryWrapper<>();

        // Exclude soft-deleted plans
        wrapper.isNull(Plan::getDeletedAt);

        // Data permission: employees see own, leaders see subordinates, admin sees all
        List<Long> visibleUserIds = userService.getVisibleUserIds(currentUserId, currentRole);
        if (visibleUserIds != null) {
            wrapper.in(Plan::getUserId, visibleUserIds);
        }

        // Apply filters
        if (StringUtils.hasText(planType)) {
            wrapper.eq(Plan::getPlanType, planType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Plan::getStatus, status);
        }
        if (StringUtils.hasText(priority)) {
            wrapper.eq(Plan::getPriority, priority);
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(Plan::getStartTime, startDate + " 00:00:00");
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(Plan::getStartTime, endDate + " 23:59:59");
        }
        if (categoryId != null) {
            wrapper.eq(Plan::getCategoryId, categoryId);
        }
        if (userId != null) {
            wrapper.eq(Plan::getUserId, userId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Plan::getTitle, keyword);
        }

        wrapper.orderByDesc(Plan::getCreatedAt);

        IPage<Plan> resultPage = planMapper.selectPage(pageParam, wrapper);

        // Convert to VO with user names
        List<Long> userIds = resultPage.getRecords().stream()
                .map(Plan::getUserId).distinct().collect(Collectors.toList());
        Map<Long, String> userNameMap = userService.getUserNameMap(userIds);

        List<PlanVO> records = resultPage.getRecords().stream()
                .map(p -> PlanVO.builder()
                        .id(p.getId())
                        .userId(p.getUserId())
                        .userName(userNameMap.getOrDefault(p.getUserId(), "未知"))
                        .planType(p.getPlanType())
                        .title(p.getTitle())
                        .description(p.getDescription())
                        .priority(p.getPriority())
                        .status(p.getStatus())
                        .startTime(DateUtils.format(p.getStartTime()))
                        .endTime(DateUtils.format(p.getEndTime()))
                        .categoryId(p.getCategoryId())
                        .quantTarget(p.getQuantTarget())
                        .createdAt(DateUtils.format(p.getCreatedAt()))
                        .build())
                .collect(Collectors.toList());

        return PageResult.of(records, resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
    }

    /**
     * Get plan by ID with data permission check (excludes soft-deleted).
     */
    public Plan getPlanById(Long id) {
        Plan plan = planMapper.selectById(id);
        if (plan == null || plan.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "计划不存在");
        }
        // Data permission check - will be enhanced in approval module
        return plan;
    }

    /**
     * Get plan VO by ID with user name resolved.
     */
    public PlanVO getPlanVOById(Long id) {
        Plan plan = getPlanById(id);
        Map<Long, String> userNameMap = userService.getUserNameMap(List.of(plan.getUserId()));
        return PlanVO.builder()
                .id(plan.getId())
                .userId(plan.getUserId())
                .userName(userNameMap.getOrDefault(plan.getUserId(), "未知"))
                .planType(plan.getPlanType())
                .title(plan.getTitle())
                .description(plan.getDescription())
                .priority(plan.getPriority())
                .status(plan.getStatus())
                .startTime(DateUtils.format(plan.getStartTime()))
                .endTime(DateUtils.format(plan.getEndTime()))
                .categoryId(plan.getCategoryId())
                .quantTarget(plan.getQuantTarget())
                .createdAt(DateUtils.format(plan.getCreatedAt()))
                .build();
    }

    /**
     * Create a new plan (draft or direct submit).
     */
    @Transactional
    public Plan createPlan(PlanCreateRequest request) {
        Long userId = UserContext.getUserId();

        // Anti-duplicate check for non-DRAFT plans
        if (Boolean.TRUE.equals(request.getSubmitDirectly())) {
            checkDuplicatePlan(userId, request.getPlanType(), request.getStartTime());
        }

        Plan plan = new Plan();
        plan.setUserId(userId);
        plan.setPlanType(request.getPlanType());
        plan.setTitle(request.getTitle());
        plan.setDescription(request.getDescription());
        plan.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        plan.setStartTime(DateUtils.parseDateTime(request.getStartTime()));
        plan.setEndTime(DateUtils.parseDateTime(request.getEndTime()));
        plan.setCategoryId(request.getCategoryId());
        plan.setQuantTarget(request.getQuantTarget());

        if (Boolean.TRUE.equals(request.getSubmitDirectly())) {
            plan.setStatus("SUBMITTED");
        } else {
            plan.setStatus("DRAFT");
        }

        planMapper.insert(plan);
        log.info("Plan created: id={}, userId={}, title={}", plan.getId(), userId, plan.getTitle());

        // Trigger approval workflow for directly submitted plans
        if (Boolean.TRUE.equals(request.getSubmitDirectly())) {
            approvalService.startApproval(plan.getId(), "PLAN", plan.getUserId());
        }

        return plan;
    }

    /**
     * Check if a plan of the same type already exists for the user in the same period.
     */
    private void checkDuplicatePlan(Long userId, String planType, String startTime) {
        LocalDateTime start = DateUtils.parseDateTime(startTime);
        String periodStart;
        String periodEnd;

        if ("DAILY".equals(planType)) {
            periodStart = start.toLocalDate().toString() + " 00:00:00";
            periodEnd = start.toLocalDate().toString() + " 23:59:59";
        } else if ("WEEKLY".equals(planType)) {
            LocalDate startDate = start.toLocalDate();
            LocalDate weekStart = startDate.with(java.time.DayOfWeek.MONDAY);
            LocalDate weekEnd = weekStart.plusDays(6);
            periodStart = weekStart.toString() + " 00:00:00";
            periodEnd = weekEnd.toString() + " 23:59:59";
        } else if ("MONTHLY".equals(planType)) {
            LocalDate startDate = start.toLocalDate();
            LocalDate monthStart = startDate.withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            periodStart = monthStart.toString() + " 00:00:00";
            periodEnd = monthEnd.toString() + " 23:59:59";
        } else {
            return;
        }

        Long count = planMapper.selectCount(new LambdaQueryWrapper<Plan>()
                .eq(Plan::getUserId, userId)
                .eq(Plan::getPlanType, planType)
                .ge(Plan::getStartTime, periodStart)
                .le(Plan::getStartTime, periodEnd)
                .ne(Plan::getStatus, "DRAFT")
                .isNull(Plan::getDeletedAt));

        if (count > 0) {
            if ("DAILY".equals(planType)) throw new BusinessException(ErrorCode.PLAN_DUPLICATE_DAILY);
            if ("WEEKLY".equals(planType)) throw new BusinessException(ErrorCode.PLAN_DUPLICATE_WEEKLY);
            if ("MONTHLY".equals(planType)) throw new BusinessException(ErrorCode.PLAN_DUPLICATE_MONTHLY);
        }
    }

    /**
     * Update a plan (only DRAFT or REJECTED status).
     */
    @Transactional
    public Plan updatePlan(Long id, PlanCreateRequest request) {
        Plan plan = getPlanById(id);

        if (!"DRAFT".equals(plan.getStatus()) && !"REJECTED".equals(plan.getStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_NOT_EDITABLE);
        }

        plan.setPlanType(request.getPlanType());
        plan.setTitle(request.getTitle());
        plan.setDescription(request.getDescription());
        plan.setPriority(request.getPriority());
        plan.setStartTime(DateUtils.parseDateTime(request.getStartTime()));
        plan.setEndTime(DateUtils.parseDateTime(request.getEndTime()));
        plan.setCategoryId(request.getCategoryId());
        plan.setQuantTarget(request.getQuantTarget());

        planMapper.updateById(plan);
        return plan;
    }

    /**
     * Submit plan for approval.
     */
    @Transactional
    public Plan submitPlan(Long id) {
        Plan plan = getPlanById(id);

        if (!"DRAFT".equals(plan.getStatus()) && !"REJECTED".equals(plan.getStatus())) {
            throw new BusinessException(ErrorCode.PLAN_ALREADY_SUBMITTED);
        }

        plan.setStatus("SUBMITTED");
        planMapper.updateById(plan);

        // Record revision for tracking changes
        recordRevision(plan, null);

        log.info("Plan submitted: id={}", id);
        // Trigger approval workflow
        approvalService.startApproval(plan.getId(), "PLAN", plan.getUserId());
        return plan;
    }

    /**
     * Soft delete a plan (DRAFT or REJECTED status).
     */
    @Transactional
    public void deletePlan(Long id) {
        Plan plan = getPlanById(id);
        if (!"DRAFT".equals(plan.getStatus()) && !"REJECTED".equals(plan.getStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_NOT_EDITABLE);
        }
        plan.setDeletedAt(LocalDateTime.now());
        planMapper.updateById(plan);

        // Insert into recycle bin for 30-day recovery
        RecycleBin bin = new RecycleBin();
        bin.setOriginalTable("plan");
        bin.setOriginalId(plan.getId());
        bin.setTitle(plan.getTitle());
        bin.setDeletedBy(UserContext.getUserId());
        bin.setDeletedAt(LocalDateTime.now());
        bin.setCanRestoreUntil(LocalDateTime.now().plusDays(30));
        recycleBinMapper.insert(bin);

        log.info("Plan soft-deleted: id={}, title={}", id, plan.getTitle());
    }

    /**
     * Withdraw a submitted plan (only if no approver has acted).
     */
    @Transactional
    public void withdrawPlan(Long id) {
        Plan plan = getPlanById(id);
        if (!"SUBMITTED".equals(plan.getStatus()) && !"APPROVING".equals(plan.getStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_NOT_EDITABLE);
        }

        // Check if any approver has already acted
        Long actedCount = approvalRecordMapper.selectCount(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getTargetId, id)
                .eq(ApprovalRecord::getTargetType, "PLAN")
                .isNotNull(ApprovalRecord::getAction));

        if (actedCount > 0) {
            throw new BusinessException(ErrorCode.PLAN_CANNOT_WITHDRAW);
        }

        // Delete pending approval records
        approvalRecordMapper.delete(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getTargetId, id)
                .eq(ApprovalRecord::getTargetType, "PLAN"));

        // Restore to DRAFT
        plan.setStatus("DRAFT");
        planMapper.updateById(plan);
        log.info("Plan withdrawn: id={}", id);
    }

    /**
     * Get calendar data for a month.
     */
    public Map<String, List<PlanVO>> getCalendarData(int year, int month, String planType) {
        Long userId = UserContext.getUserId();
        List<Long> visibleUserIds = userService.getVisibleUserIds(userId, UserContext.getRole());

        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.plusMonths(1);

        LambdaQueryWrapper<Plan> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Plan::getDeletedAt);
        if (visibleUserIds != null) {
            wrapper.in(Plan::getUserId, visibleUserIds);
        }
        wrapper.ge(Plan::getStartTime, firstDay.toString());
        wrapper.lt(Plan::getStartTime, lastDay.toString());
        if (StringUtils.hasText(planType)) {
            wrapper.eq(Plan::getPlanType, planType);
        }
        wrapper.orderByAsc(Plan::getStartTime);

        List<Plan> plans = planMapper.selectList(wrapper);

        // Group by date
        return plans.stream()
                .map(p -> PlanVO.builder()
                        .id(p.getId())
                        .title(p.getTitle())
                        .planType(p.getPlanType())
                        .priority(p.getPriority())
                        .status(p.getStatus())
                        .startTime(DateUtils.formatDate(p.getStartTime().toLocalDate()))
                        .build())
                .collect(Collectors.groupingBy(PlanVO::getStartTime));
    }

    /**
     * Record a revision entry when a plan is submitted (tracking changes).
     */
    private void recordRevision(Plan plan, String submitterNote) {
        Long count = planRevisionMapper.selectCount(new LambdaQueryWrapper<PlanRevision>()
                .eq(PlanRevision::getPlanId, plan.getId()));
        int version = (int) (count + 1);

        PlanRevision revision = new PlanRevision();
        revision.setPlanId(plan.getId());
        revision.setVersion(version);
        revision.setSubmitterNote(submitterNote);
        revision.setCreatedAt(LocalDateTime.now());
        planRevisionMapper.insert(revision);
    }

    /**
     * Get revision history for a plan.
     */
    public List<PlanRevision> getRevisions(Long planId) {
        return planRevisionMapper.selectList(new LambdaQueryWrapper<PlanRevision>()
                .eq(PlanRevision::getPlanId, planId)
                .orderByAsc(PlanRevision::getVersion));
    }

    /**
     * Copy a plan as a new DRAFT.
     */
    @Transactional
    public Plan copyPlan(Long id) {
        Plan original = getPlanById(id);
        Plan copy = new Plan();
        copy.setUserId(UserContext.getUserId());
        copy.setPlanType(original.getPlanType());
        copy.setTitle(original.getTitle() + " (副本)");
        copy.setDescription(original.getDescription());
        copy.setPriority(original.getPriority());
        copy.setStartTime(original.getStartTime());
        copy.setEndTime(original.getEndTime());
        copy.setCategoryId(original.getCategoryId());
        copy.setQuantTarget(original.getQuantTarget());
        copy.setStatus("DRAFT");
        planMapper.insert(copy);
        log.info("Plan copied: originalId={}, newId={}", id, copy.getId());
        return copy;
    }

    /**
     * Get approved plans that have no achievement submitted yet.
     * Used for the "submit achievement" quick-entry list.
     */
    public List<PlanVO> getApprovedPlansWithoutAchievement() {
        Long userId = UserContext.getUserId();
        List<Plan> plans = planMapper.selectList(new LambdaQueryWrapper<Plan>()
                .eq(Plan::getUserId, userId)
                .eq(Plan::getStatus, "APPROVED")
                .isNull(Plan::getDeletedAt)
                .orderByDesc(Plan::getCreatedAt));

        Map<Long, String> userNameMap = userService.getUserNameMap(List.of(userId));

        // Filter out plans that already have an achievement
        return plans.stream()
                .filter(p -> {
                    Long achvCount = achievementPlanRefMapper.selectCount(
                            new LambdaQueryWrapper<AchievementPlanRef>()
                                    .eq(AchievementPlanRef::getPlanId, p.getId()));
                    // Also check direct achievement link
                    if (achvCount == 0) {
                        achvCount = achievementMapper.selectCount(
                                new LambdaQueryWrapper<Achievement>()
                                        .eq(Achievement::getPlanId, p.getId())
                                        .isNull(Achievement::getDeletedAt));
                    }
                    return achvCount == 0;
                })
                .map(p -> PlanVO.builder()
                        .id(p.getId())
                        .userId(p.getUserId())
                        .userName(userNameMap.getOrDefault(p.getUserId(), "未知"))
                        .planType(p.getPlanType())
                        .title(p.getTitle())
                        .priority(p.getPriority())
                        .status(p.getStatus())
                        .startTime(DateUtils.format(p.getStartTime()))
                        .endTime(DateUtils.format(p.getEndTime()))
                        .createdAt(DateUtils.format(p.getCreatedAt()))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get rollup options (daily plans for weekly, weekly plans for monthly).
     */
    public List<PlanVO> getRollupOptions(Long planId, String planType, String startDate, String endDate) {
        Long userId = UserContext.getUserId();

        String childType;
        if ("WEEKLY".equals(planType)) {
            childType = "DAILY";
        } else if ("MONTHLY".equals(planType)) {
            childType = "WEEKLY";
        } else {
            return List.of();
        }

        LambdaQueryWrapper<Plan> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Plan::getDeletedAt)
                .eq(Plan::getUserId, userId)
                .eq(Plan::getPlanType, childType)
                .eq(Plan::getStatus, "APPROVED")
                .ge(Plan::getStartTime, startDate + " 00:00:00")
                .le(Plan::getStartTime, endDate + " 23:59:59")
                .orderByAsc(Plan::getStartTime);

        return planMapper.selectList(wrapper).stream()
                .map(p -> PlanVO.builder()
                        .id(p.getId())
                        .planType(p.getPlanType())
                        .title(p.getTitle())
                        .status(p.getStatus())
                        .startTime(DateUtils.formatDate(p.getStartTime().toLocalDate()))
                        .build())
                .collect(Collectors.toList());
    }
}
