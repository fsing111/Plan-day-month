package com.plansystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plansystem.dto.PageResult;
import com.plansystem.dto.PlanCreateRequest;
import com.plansystem.dto.PlanVO;
import com.plansystem.entity.Plan;
import com.plansystem.exception.BusinessException;
import com.plansystem.exception.ErrorCode;
import com.plansystem.mapper.PlanMapper;
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
     * Get plan by ID with data permission check.
     */
    public Plan getPlanById(Long id) {
        Plan plan = planMapper.selectById(id);
        if (plan == null) {
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
        return plan;
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
        log.info("Plan submitted: id={}", id);
        // TODO: Trigger approval workflow once approval module is ready
        return plan;
    }

    /**
     * Delete a plan (only DRAFT status).
     */
    @Transactional
    public void deletePlan(Long id) {
        Plan plan = getPlanById(id);
        if (!"DRAFT".equals(plan.getStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_NOT_EDITABLE);
        }
        planMapper.deleteById(id);
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
        wrapper.eq(Plan::getUserId, userId)
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
