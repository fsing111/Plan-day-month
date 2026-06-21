package com.ruoyi.approval.mapper;

import java.util.List;
import java.util.Map;

import com.ruoyi.approval.domain.ApprovalChain;

public interface ApprovalChainMapper {
    List<ApprovalChain> selectApprovalChainList(ApprovalChain chain);
    List<ApprovalChain> selectByDeptAndType(Map<String, Object> params);
    ApprovalChain selectById(Long id);
    int insertApprovalChain(ApprovalChain chain);
    int updateApprovalChain(ApprovalChain chain);
    int deleteById(Long id);
}
