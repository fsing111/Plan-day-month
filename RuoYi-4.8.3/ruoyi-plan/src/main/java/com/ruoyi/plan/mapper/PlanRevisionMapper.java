package com.ruoyi.plan.mapper;

import java.util.List;

import com.ruoyi.plan.domain.PlanRevision;

/**
 * 计划修订版本Mapper
 */
public interface PlanRevisionMapper {

    List<PlanRevision> selectPlanRevisionByPlanId(Long planId);

    int selectMaxVersionByPlanId(Long planId);

    int insertPlanRevision(PlanRevision planRevision);
}
