package com.ruoyi.approval.service;

import java.util.List;
import java.util.Map;

import com.ruoyi.approval.domain.ApprovalRecord;

public interface IApprovalService {
    List<ApprovalRecord> selectPendingList(ApprovalRecord record);
    List<ApprovalRecord> selectHistoryList(ApprovalRecord record);
    List<ApprovalRecord> getTimeline(String targetType, Long targetId);
    void startApproval(String targetType, Long targetId, String planType, Long submitterDeptId, Long submitterId);
    void approve(Long recordId, String comment);
    void reject(Long recordId, String comment);
    void transfer(Long recordId, Long targetUserId, String comment);
    Map<String, Object> batchApprove(List<Long> recordIds, String comment);
}
