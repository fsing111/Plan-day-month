package com.ruoyi.plan.service;

import java.util.List;
import java.util.Map;

import com.ruoyi.plan.domain.Plan;
import com.ruoyi.plan.domain.PlanRevision;

/**
 * 计划Service接口
 *
 * @author ruoyi
 */
public interface IPlanService {

    /**
     * 查询计划列表
     */
    List<Plan> selectPlanList(Plan plan);

    /**
     * 根据ID查询计划详情
     */
    Plan selectPlanById(Long id);

    /**
     * 新增计划
     */
    int insertPlan(Plan plan);

    /**
     * 修改计划（仅DRAFT/REJECTED状态可编辑）
     */
    int updatePlan(Plan plan);

    /**
     * 提交计划
     */
    void submitPlan(Long planId);

    /**
     * 撤回计划（SUBMITTED/APPROVING→DRAFT）
     */
    void withdrawPlan(Long planId);

    /**
     * 软删除计划
     */
    void deletePlan(Long planId);

    /**
     * 复制计划
     */
    Plan copyPlan(Long sourceId);

    /**
     * 获取日历视图数据
     */
    Map<String, List<Plan>> getCalendarData(Integer year, Integer month, String planType);

    /**
     * 获取可汇总引用的子计划列表
     */
    List<Plan> getRollupOptions(Long planId, String planType, String startDate, String endDate);

    /**
     * 获取计划的修改版本历史
     */
    List<PlanRevision> getPlanRevisions(Long planId);

    /**
     * 从模板创建计划
     */
    Plan createFromTemplate(Long templateId);
}
