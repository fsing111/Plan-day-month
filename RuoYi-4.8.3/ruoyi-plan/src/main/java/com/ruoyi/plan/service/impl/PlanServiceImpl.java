package com.ruoyi.plan.service.impl;

import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.constant.PlanConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.plan.domain.Plan;
import com.ruoyi.plan.domain.PlanRevision;
import com.ruoyi.plan.domain.PlanTemplate;
import com.ruoyi.plan.mapper.PlanMapper;
import com.ruoyi.plan.mapper.PlanRevisionMapper;
import com.ruoyi.plan.mapper.PlanTemplateMapper;
import com.ruoyi.plan.service.IPlanService;

/**
 * 计划Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class PlanServiceImpl implements IPlanService {
    private static final Logger log = LoggerFactory.getLogger(PlanServiceImpl.class);

    @Autowired
    private PlanMapper planMapper;

    @Autowired
    private PlanRevisionMapper planRevisionMapper;

    @Autowired
    private PlanTemplateMapper planTemplateMapper;

    // ==================== 查询 ====================

    @Override
    public List<Plan> selectPlanList(Plan plan) {
        return planMapper.selectPlanList(plan);
    }

    @Override
    public Plan selectPlanById(Long id) {
        return planMapper.selectPlanById(id);
    }

    // ==================== 增删改 ====================

    @Override
    @Transactional
    public int insertPlan(Plan plan) {
        plan.setUserId(ShiroUtils.getUserId());
        plan.setCreateBy(ShiroUtils.getLoginName());
        plan.setStatus(PlanConstants.STATUS_DRAFT);
        return planMapper.insertPlan(plan);
    }

    @Override
    @Transactional
    public int updatePlan(Plan plan) {
        Plan existing = planMapper.selectPlanById(plan.getId());
        if (existing == null) {
            throw new ServiceException("计划不存在");
        }
        // 仅草稿和待修改状态可编辑
        if (!PlanConstants.STATUS_DRAFT.equals(existing.getStatus())
                && !PlanConstants.STATUS_REJECTED.equals(existing.getStatus())) {
            throw new ServiceException("当前状态不允许编辑");
        }
        plan.setUpdateBy(ShiroUtils.getLoginName());
        return planMapper.updatePlan(plan);
    }

    // ==================== 提交 ====================

    @Override
    @Transactional
    public void submitPlan(Long planId) {
        Plan plan = planMapper.selectPlanById(planId);
        if (plan == null) {
            throw new ServiceException("计划不存在");
        }
        if (!PlanConstants.STATUS_DRAFT.equals(plan.getStatus())
                && !PlanConstants.STATUS_REJECTED.equals(plan.getStatus())) {
            throw new ServiceException("当前状态不允许提交，仅草稿和待修改状态可提交");
        }

        // 防重复检查
        checkDuplicatePlan(plan);

        // 更新状态为已提交
        Map<String, Object> params = new HashMap<>();
        params.put("id", planId);
        params.put("status", PlanConstants.STATUS_SUBMITTED);
        planMapper.updatePlanStatus(params);

        // 记录修订版本
        int maxVersion = planRevisionMapper.selectMaxVersionByPlanId(planId);
        PlanRevision revision = new PlanRevision();
        revision.setPlanId(planId);
        revision.setVersion(maxVersion + 1);
        revision.setCreateBy(ShiroUtils.getLoginName());
        planRevisionMapper.insertPlanRevision(revision);
    }

    // ==================== 撤回 ====================

    @Override
    @Transactional
    public void withdrawPlan(Long planId) {
        Plan plan = planMapper.selectPlanById(planId);
        if (plan == null) {
            throw new ServiceException("计划不存在");
        }
        if (!PlanConstants.STATUS_SUBMITTED.equals(plan.getStatus())
                && !PlanConstants.STATUS_APPROVING.equals(plan.getStatus())) {
            throw new ServiceException("当前状态不允许撤回，仅已提交和审批中状态可撤回");
        }

        // 撤回时需要清除审批记录（由 ApprovalService 调用方处理）
        Map<String, Object> params = new HashMap<>();
        params.put("id", planId);
        params.put("status", PlanConstants.STATUS_DRAFT);
        planMapper.updatePlanStatus(params);
    }

    // ==================== 删除 ====================

    @Override
    @Transactional
    public void deletePlan(Long planId) {
        Plan plan = planMapper.selectPlanById(planId);
        if (plan == null) {
            throw new ServiceException("计划不存在");
        }
        if (!PlanConstants.STATUS_DRAFT.equals(plan.getStatus())) {
            throw new ServiceException("仅草稿状态的计划可以删除");
        }
        planMapper.deletePlanById(planId);
    }

    // ==================== 复制 ====================

    @Override
    @Transactional
    public Plan copyPlan(Long sourceId) {
        Plan source = planMapper.selectPlanById(sourceId);
        if (source == null) {
            throw new ServiceException("原计划不存在");
        }
        Plan copy = new Plan();
        copy.setPlanType(source.getPlanType());
        copy.setTitle(source.getTitle() + "（副本）");
        copy.setDescription(source.getDescription());
        copy.setPriority(source.getPriority());
        copy.setStartTime(source.getStartTime());
        copy.setEndTime(source.getEndTime());
        copy.setCategoryId(source.getCategoryId());
        copy.setQuantTarget(source.getQuantTarget());
        copy.setUserId(ShiroUtils.getUserId());
        copy.setCreateBy(ShiroUtils.getLoginName());
        copy.setStatus(PlanConstants.STATUS_DRAFT);
        planMapper.insertPlan(copy);
        return copy;
    }

    // ==================== 日历视图 ====================

    @Override
    public Map<String, List<Plan>> getCalendarData(Integer year, Integer month, String planType) {
        Map<String, Object> params = new HashMap<>();
        params.put("year", year);
        params.put("month", month);
        params.put("planType", planType);

        List<Plan> plans = planMapper.selectCalendarPlans(params);
        Map<String, List<Plan>> result = new LinkedHashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Plan p : plans) {
            String dateKey = sdf.format(p.getStartTime());
            result.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(p);
        }
        return result;
    }

    // ==================== 汇总引用 ====================

    @Override
    public List<Plan> getRollupOptions(Long planId, String planType, String startDate, String endDate) {
        String subType;
        if (PlanConstants.TYPE_WEEKLY.equals(planType)) {
            subType = PlanConstants.TYPE_DAILY;
        } else if (PlanConstants.TYPE_MONTHLY.equals(planType)) {
            subType = PlanConstants.TYPE_WEEKLY;
        } else {
            return Collections.emptyList();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("userId", ShiroUtils.getUserId());
        params.put("subType", subType);
        params.put("startDate", startDate);
        params.put("endDate", endDate);

        return planMapper.selectApprovedSubPlans(params);
    }

    // ==================== 修订版本 ====================

    @Override
    public List<PlanRevision> getPlanRevisions(Long planId) {
        return planRevisionMapper.selectPlanRevisionByPlanId(planId);
    }

    // ==================== 模板创建 ====================

    @Override
    @Transactional
    public Plan createFromTemplate(Long templateId) {
        PlanTemplate template = planTemplateMapper.selectPlanTemplateById(templateId);
        if (template == null) {
            throw new ServiceException("模板不存在");
        }
        Plan plan = new Plan();
        plan.setPlanType(template.getPlanType());
        plan.setTitle(template.getTitle());
        plan.setDescription(template.getDescription());
        plan.setPriority(template.getPriority());
        plan.setCategoryId(template.getCategoryId());
        plan.setQuantTarget(template.getQuantTarget());
        plan.setUserId(ShiroUtils.getUserId());
        plan.setCreateBy(ShiroUtils.getLoginName());
        plan.setStatus(PlanConstants.STATUS_DRAFT);

        // 设置默认时间范围
        Calendar now = Calendar.getInstance();
        plan.setStartTime(now.getTime());
        if (PlanConstants.TYPE_DAILY.equals(template.getPlanType())) {
            now.set(Calendar.HOUR_OF_DAY, 23);
            now.set(Calendar.MINUTE, 59);
        } else if (PlanConstants.TYPE_WEEKLY.equals(template.getPlanType())) {
            now.add(Calendar.DAY_OF_MONTH, 6);
        } else {
            now.add(Calendar.MONTH, 1);
        }
        plan.setEndTime(now.getTime());

        planMapper.insertPlan(plan);
        return plan;
    }

    // ==================== 私有方法 ====================

    /**
     * 防重复提交检查：同一用户+同一天/周/月不能有重复计划
     */
    private void checkDuplicatePlan(Plan plan) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(plan.getStartTime());
        Date startDate, endDate;

        if (PlanConstants.TYPE_DAILY.equals(plan.getPlanType())) {
            startDate = DateUtils.getDayStart(plan.getStartTime());
            endDate = DateUtils.getDayEnd(plan.getStartTime());
        } else if (PlanConstants.TYPE_WEEKLY.equals(plan.getPlanType())) {
            startDate = DateUtils.getWeekStart(plan.getStartTime());
            endDate = DateUtils.getWeekEnd(plan.getStartTime());
        } else {
            startDate = DateUtils.getMonthStart(plan.getStartTime());
            endDate = DateUtils.getMonthEnd(plan.getStartTime());
        }

        Map<String, Object> params = new HashMap<>();
        params.put("userId", ShiroUtils.getUserId());
        params.put("planType", plan.getPlanType());
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("excludeId", plan.getId() != null ? plan.getId() : 0L);
        int count = planMapper.countDuplicatePlan(params);
        if (count > 0) {
            String typeLabel = PlanConstants.TYPE_DAILY.equals(plan.getPlanType()) ? "日报"
                    : PlanConstants.TYPE_WEEKLY.equals(plan.getPlanType()) ? "周报" : "月报";
            throw new ServiceException("您在该时间段内已有一份" + typeLabel + "，请勿重复提交");
        }
    }
}
