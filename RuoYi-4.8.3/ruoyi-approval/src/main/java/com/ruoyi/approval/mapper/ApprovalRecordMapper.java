package com.ruoyi.approval.mapper;

import java.util.List;
import java.util.Map;

import com.ruoyi.approval.domain.ApprovalRecord;

public interface ApprovalRecordMapper {
    List<ApprovalRecord> selectPendingList(ApprovalRecord record);
    List<ApprovalRecord> selectHistoryList(ApprovalRecord record);
    List<ApprovalRecord> selectByTarget(Map<String, Object> params);
    ApprovalRecord selectById(Long id);
    int insertApprovalRecord(ApprovalRecord record);
    int updateApprovalRecord(ApprovalRecord record);
    int deleteByTarget(Map<String, Object> params);
    int countPendingByLevel(Map<String, Object> params);
    int countLevelsAbove(Map<String, Object> params);
    int batchInsert(List<ApprovalRecord> records);
}
