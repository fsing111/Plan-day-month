package com.plansystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plansystem.entity.PlanTemplate;
import com.plansystem.exception.BusinessException;
import com.plansystem.exception.ErrorCode;
import com.plansystem.mapper.PlanTemplateMapper;
import com.plansystem.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Plan template service — save and reuse plan templates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanTemplateService {

    private final PlanTemplateMapper templateMapper;

    public List<PlanTemplate> getUserTemplates() {
        Long userId = UserContext.getUserId();
        return templateMapper.selectList(new LambdaQueryWrapper<PlanTemplate>()
                .eq(PlanTemplate::getUserId, userId)
                .orderByDesc(PlanTemplate::getCreatedAt));
    }

    @Transactional
    public PlanTemplate createTemplate(PlanTemplate template) {
        template.setUserId(UserContext.getUserId());
        templateMapper.insert(template);
        log.info("Template created: id={}, name={}", template.getId(), template.getName());
        return template;
    }

    @Transactional
    public void deleteTemplate(Long id) {
        PlanTemplate t = templateMapper.selectById(id);
        if (t == null) throw new BusinessException(ErrorCode.NOT_FOUND, "模板不存在");
        templateMapper.deleteById(id);
    }
}
