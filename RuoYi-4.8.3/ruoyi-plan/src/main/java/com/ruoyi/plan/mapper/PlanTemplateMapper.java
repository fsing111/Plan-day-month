package com.ruoyi.plan.mapper;

import java.util.List;

import com.ruoyi.plan.domain.PlanTemplate;

/**
 * 计划模板Mapper
 */
public interface PlanTemplateMapper {

    List<PlanTemplate> selectPlanTemplateList(PlanTemplate template);

    PlanTemplate selectPlanTemplateById(Long id);

    int insertPlanTemplate(PlanTemplate template);

    int deletePlanTemplateById(Long id);
}
