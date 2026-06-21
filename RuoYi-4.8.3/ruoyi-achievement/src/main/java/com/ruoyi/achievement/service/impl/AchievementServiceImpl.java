package com.ruoyi.achievement.service.impl;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.achievement.domain.Achievement;
import com.ruoyi.achievement.domain.AchievementPlanRef;
import com.ruoyi.achievement.domain.Attachment;
import com.ruoyi.achievement.mapper.AchievementMapper;
import com.ruoyi.achievement.mapper.AchievementPlanRefMapper;
import com.ruoyi.achievement.mapper.AttachmentMapper;
import com.ruoyi.achievement.service.IAchievementService;
import com.ruoyi.common.constant.AchievementConstants;
import com.ruoyi.common.constant.PlanConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.plan.domain.Plan;
import com.ruoyi.plan.mapper.PlanMapper;

@Service
public class AchievementServiceImpl implements IAchievementService {

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private AchievementPlanRefMapper refMapper;

    @Autowired
    private AttachmentMapper attachmentMapper;

    @Autowired
    private PlanMapper planMapper;

    @Override
    public List<Achievement> selectAchievementList(Achievement achievement) {
        List<Achievement> list = achievementMapper.selectAchievementList(achievement);
        for (Achievement a : list) {
            List<AchievementPlanRef> refs = refMapper.selectByAchievementId(a.getId());
            a.setPlanIds(refs.stream().map(AchievementPlanRef::getPlanId).toList());
            List<Attachment> atts = attachmentMapper.selectByAchievementId(a.getId());
            a.setAttachments(atts);
        }
        return list;
    }

    @Override
    public Achievement selectAchievementById(Long id) {
        Achievement a = achievementMapper.selectAchievementById(id);
        if (a != null) {
            List<AchievementPlanRef> refs = refMapper.selectByAchievementId(id);
            a.setPlanIds(refs.stream().map(AchievementPlanRef::getPlanId).toList());
            a.setAttachments(attachmentMapper.selectByAchievementId(id));
        }
        return a;
    }

    @Override
    @Transactional
    public int insertAchievement(Achievement achievement) {
        achievement.setCreateBy(ShiroUtils.getLoginName());
        achievement.setStatus(AchievementConstants.STATUS_PENDING);

        // 校验所有关联计划必须为 APPROVED
        List<Long> planIds = achievement.getPlanIds();
        if (planIds != null && !planIds.isEmpty()) {
            for (Long planId : planIds) {
                Plan plan = planMapper.selectPlanById(planId);
                if (plan == null || !PlanConstants.STATUS_APPROVED.equals(plan.getStatus())) {
                    throw new ServiceException("计划[" + planId + "]未通过审批，不能创建成果");
                }
            }
            achievement.setPlanId(planIds.get(0)); // 主关联第一个计划
        }

        int rows = achievementMapper.insertAchievement(achievement);

        // 批量插入关联记录
        if (planIds != null && !planIds.isEmpty()) {
            List<AchievementPlanRef> refs = new ArrayList<>();
            for (Long planId : planIds) {
                AchievementPlanRef ref = new AchievementPlanRef();
                ref.setAchievementId(achievement.getId());
                ref.setPlanId(planId);
                ref.setCreateBy(ShiroUtils.getLoginName());
                refs.add(ref);
            }
            refMapper.batchInsert(refs);
        }

        return rows;
    }

    @Override
    @Transactional
    public int updateAchievement(Achievement achievement) {
        Achievement existing = achievementMapper.selectAchievementById(achievement.getId());
        if (existing == null) throw new ServiceException("成果不存在");
        if (!AchievementConstants.STATUS_PENDING.equals(existing.getStatus())
                && !AchievementConstants.STATUS_REJECTED.equals(existing.getStatus())) {
            throw new ServiceException("当前状态不允许编辑");
        }
        achievement.setUpdateBy(ShiroUtils.getLoginName());
        return achievementMapper.updateAchievement(achievement);
    }

    @Override
    @Transactional
    public void submitAchievement(Long achievementId) {
        Achievement a = achievementMapper.selectAchievementById(achievementId);
        if (a == null) throw new ServiceException("成果不存在");
        if (!AchievementConstants.STATUS_PENDING.equals(a.getStatus())
                && !AchievementConstants.STATUS_REJECTED.equals(a.getStatus())) {
            throw new ServiceException("当前状态不允许提交");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("id", achievementId);
        params.put("status", AchievementConstants.STATUS_SUBMITTED);
        params.put("submittedAt", new Date());
        achievementMapper.updateAchievementStatus(params);
    }

    @Override
    @Transactional
    public void withdrawAchievement(Long achievementId) {
        Achievement a = achievementMapper.selectAchievementById(achievementId);
        if (a == null) throw new ServiceException("成果不存在");
        if (!AchievementConstants.STATUS_SUBMITTED.equals(a.getStatus())
                && !AchievementConstants.STATUS_APPROVING.equals(a.getStatus())) {
            throw new ServiceException("当前状态不允许撤回");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("id", achievementId);
        params.put("status", AchievementConstants.STATUS_PENDING);
        achievementMapper.updateAchievementStatus(params);
    }

    @Override
    @Transactional
    public void deleteAchievement(Long achievementId) {
        Achievement a = achievementMapper.selectAchievementById(achievementId);
        if (a == null) throw new ServiceException("成果不存在");
        if (!AchievementConstants.STATUS_PENDING.equals(a.getStatus())) {
            throw new ServiceException("仅待填写状态的成果可删除");
        }
        achievementMapper.deleteAchievementById(achievementId);
    }

    @Override
    public String getComparisonStatus(Long achievementId) {
        Achievement a = achievementMapper.selectAchievementById(achievementId);
        if (a == null) return null;
        Plan p = planMapper.selectPlanById(a.getPlanId());
        if (p == null || p.getQuantTarget() == null) return AchievementConstants.COMPARISON_MATCH;
        if (a.getActualQty() == null) return AchievementConstants.COMPARISON_NOT_MATCH;

        try {
            double target = Double.parseDouble(p.getQuantTarget());
            double actual = Double.parseDouble(a.getActualQty());
            if (actual >= target * 1.2) return AchievementConstants.COMPARISON_EXCEED;
            if (actual >= target) return AchievementConstants.COMPARISON_MATCH;
            if (actual >= target * 0.5) return AchievementConstants.COMPARISON_PARTIAL;
            return AchievementConstants.COMPARISON_NOT_MATCH;
        } catch (NumberFormatException e) {
            return AchievementConstants.COMPARISON_MATCH;
        }
    }
}
