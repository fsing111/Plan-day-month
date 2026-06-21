package com.ruoyi.plan.mapper;

import java.util.List;
import java.util.Map;

import com.ruoyi.plan.domain.Plan;

/**
 * 计划Mapper接口
 *
 * @author ruoyi
 */
public interface PlanMapper {

    /**
     * 查询计划列表
     */
    List<Plan> selectPlanList(Plan plan);

    /**
     * 根据ID查询计划
     */
    Plan selectPlanById(Long id);

    /**
     * 新增计划
     */
    int insertPlan(Plan plan);

    /**
     * 修改计划
     */
    int updatePlan(Plan plan);

    /**
     * 软删除计划
     */
    int deletePlanById(Long id);

    /**
     * 获取日历视图数据
     */
    List<Plan> selectCalendarPlans(Map<String, Object> params);

    /**
     * 获取审批通过的子计划（用于汇总引用）
     */
    List<Plan> selectApprovedSubPlans(Map<String, Object> params);

    /**
     * 根据用户ID和日期范围查询APPROVED状态的计划
     */
    List<Plan> selectApprovedByUserAndDateRange(Map<String, Object> params);

    /**
     * 查询用户某天/周/月是否已有非DRAFT状态的同类型计划（防重复）
     */
    int countDuplicatePlan(Map<String, Object> params);

    /**
     * 更新计划状态
     */
    int updatePlanStatus(Map<String, Object> params);

    /**
     * 批量归档：将end_time已过的APPROVED计划标记为ARCHIVED
     */
    int batchArchivePlans();

    /**
     * 批量标记逾期：将end_time已过且无成果的APPROVED计划标记为OVERDUE
     */
    int batchMarkOverdue();
}
