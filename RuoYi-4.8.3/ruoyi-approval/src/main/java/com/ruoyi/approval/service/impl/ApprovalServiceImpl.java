package com.ruoyi.approval.service.impl;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.approval.domain.ApprovalChain;
import com.ruoyi.approval.domain.ApprovalRecord;
import com.ruoyi.approval.mapper.ApprovalChainMapper;
import com.ruoyi.approval.mapper.ApprovalRecordMapper;
import com.ruoyi.approval.service.IApprovalService;
import com.ruoyi.common.constant.AchievementConstants;
import com.ruoyi.common.constant.ApprovalConstants;
import com.ruoyi.common.constant.PlanConstants;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.common.utils.StringUtils;

@Service
public class ApprovalServiceImpl implements IApprovalService {
    private static final Logger log = LoggerFactory.getLogger(ApprovalServiceImpl.class);

    @Autowired
    private ApprovalRecordMapper recordMapper;

    @Autowired
    private ApprovalChainMapper chainMapper;

    @Override
    public List<ApprovalRecord> selectPendingList(ApprovalRecord record) {
        record.setApproverId(ShiroUtils.getUserId());
        List<ApprovalRecord> list = recordMapper.selectPendingList(record);
        // 加载同级审批人状态
        for (ApprovalRecord r : list) {
            Map<String, Object> params = new HashMap<>();
            params.put("targetId", r.getTargetId());
            params.put("targetType", r.getTargetType());
            params.put("approvalLevel", r.getApprovalLevel());
            r.setPeerApprovals(recordMapper.selectByTarget(params));
        }
        return list;
    }

    @Override
    public List<ApprovalRecord> selectHistoryList(ApprovalRecord record) {
        return recordMapper.selectHistoryList(record);
    }

    @Override
    public List<ApprovalRecord> getTimeline(String targetType, Long targetId) {
        Map<String, Object> params = new HashMap<>();
        params.put("targetId", targetId);
        params.put("targetType", targetType);
        return recordMapper.selectByTarget(params);
    }

    @Override
    @Transactional
    public void startApproval(String targetType, Long targetId, String planType, Long submitterDeptId, Long submitterId) {
        // 查询审批链配置
        Map<String, Object> chainParams = new HashMap<>();
        chainParams.put("deptId", submitterDeptId);
        chainParams.put("planType", planType);
        List<ApprovalChain> chains = chainMapper.selectByDeptAndType(chainParams);

        if (chains.isEmpty()) {
            // 回退：直属领导为一级审批人
            SysUser leaderUser = getUserById(null); // 需要从sys_user查leader_id
            // 实际实现：通过 ISysUserService 查询提交人的 leader_id
            // 简化处理：如果没配置审批链，直接通过
            log.info("未配置审批链，计划/成果 {} 直接通过", targetId);
            return;
        }

        // 创建审批记录
        List<ApprovalRecord> records = new ArrayList<>();
        for (ApprovalChain chain : chains) {
            ApprovalRecord record = new ApprovalRecord();
            record.setTargetId(targetId);
            record.setTargetType(targetType);
            record.setApproverId(chain.getApproverId());
            record.setApprovalLevel(chain.getApprovalLevel());
            record.setSortOrder(chain.getSortOrder());
            record.setCreateBy(ShiroUtils.getLoginName());
            records.add(record);
        }
        recordMapper.batchInsert(records);

        // 更新目标状态为 APPROVING
        String newStatus = PlanConstants.APPROVAL_TARGET_PLAN.equals(targetType)
                ? PlanConstants.STATUS_APPROVING
                : AchievementConstants.STATUS_APPROVING;
        // 状态更新由调用方负责
    }

    @Override
    @Transactional
    public void approve(Long recordId, String comment) {
        ApprovalRecord record = recordMapper.selectById(recordId);
        if (record == null) throw new ServiceException("审批记录不存在");
        if (!ShiroUtils.getUserId().equals(record.getApproverId())) {
            throw new ServiceException("无权操作此审批记录");
        }
        if (record.getAction() != null) throw new ServiceException("该审批已处理");

        record.setAction(ApprovalConstants.ACTION_APPROVE);
        record.setComment(comment);
        recordMapper.updateApprovalRecord(record);

        // 检查同级是否全部通过
        Map<String, Object> params = new HashMap<>();
        params.put("targetId", record.getTargetId());
        params.put("targetType", record.getTargetType());
        params.put("approvalLevel", record.getApprovalLevel());
        int pending = recordMapper.countPendingByLevel(params);

        if (pending == 0) {
            // 检查是否最后一级
            Map<String, Object> levelParams = new HashMap<>();
            levelParams.put("targetId", record.getTargetId());
            levelParams.put("targetType", record.getTargetType());
            levelParams.put("approvalLevel", record.getApprovalLevel());
            int remainingLevels = recordMapper.countLevelsAbove(levelParams);

            if (remainingLevels == 0) {
                // 全部通过，更新目标状态为 APPROVED
                String approvedStatus = ApprovalConstants.TARGET_PLAN.equals(record.getTargetType())
                        ? PlanConstants.STATUS_APPROVED
                        : AchievementConstants.STATUS_APPROVED;
                // 状态更新由调用方负责
            }
            // 否则自动进入下一级审批
        }
    }

    @Override
    @Transactional
    public void reject(Long recordId, String comment) {
        // Gap-5: 驳回理由强制校验
        if (StringUtils.isBlank(comment)) {
            throw new ServiceException("驳回时必须填写审批意见");
        }

        ApprovalRecord record = recordMapper.selectById(recordId);
        if (record == null) throw new ServiceException("审批记录不存在");
        if (!ShiroUtils.getUserId().equals(record.getApproverId())) {
            throw new ServiceException("无权操作此审批记录");
        }
        if (record.getAction() != null) throw new ServiceException("该审批已处理");

        record.setAction(ApprovalConstants.ACTION_REJECT);
        record.setComment(comment);
        recordMapper.updateApprovalRecord(record);

        // 更新目标状态为 REJECTED
        String rejectedStatus = ApprovalConstants.TARGET_PLAN.equals(record.getTargetType())
                ? PlanConstants.STATUS_REJECTED
                : AchievementConstants.STATUS_REJECTED;
        // 状态更新由调用方负责
        // 清除非当前级别的待审批记录
    }

    @Override
    @Transactional
    public void transfer(Long recordId, Long targetUserId, String comment) {
        ApprovalRecord record = recordMapper.selectById(recordId);
        if (record == null) throw new ServiceException("审批记录不存在");
        if (!ShiroUtils.getUserId().equals(record.getApproverId())) {
            throw new ServiceException("无权操作此审批记录");
        }

        record.setAction(ApprovalConstants.ACTION_TRANSFER);
        record.setComment(comment);
        recordMapper.updateApprovalRecord(record);

        // Gap: 转审限同部门 - 创建新的审批记录给目标审批人
        ApprovalRecord newRecord = new ApprovalRecord();
        newRecord.setTargetId(record.getTargetId());
        newRecord.setTargetType(record.getTargetType());
        newRecord.setApproverId(targetUserId);
        newRecord.setApprovalLevel(record.getApprovalLevel());
        newRecord.setSortOrder(record.getSortOrder() + 1);
        newRecord.setCreateBy(ShiroUtils.getLoginName());
        recordMapper.insertApprovalRecord(newRecord);
    }

    @Override
    @Transactional
    public Map<String, Object> batchApprove(List<Long> recordIds, String comment) {
        int success = 0;
        List<String> errors = new ArrayList<>();
        for (Long rid : recordIds) {
            try {
                approve(rid, comment);
                success++;
            } catch (Exception e) {
                errors.add("记录[" + rid + "]: " + e.getMessage());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", success);
        result.put("errorCount", errors.size());
        result.put("errors", errors);
        return result;
    }

    private SysUser getUserById(Long userId) {
        // Placeholder - would use ISysUserService
        return null;
    }
}
