package com.plansystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plansystem.entity.*;
import com.plansystem.exception.BusinessException;
import com.plansystem.exception.ErrorCode;
import com.plansystem.mapper.*;
import com.plansystem.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalRecordMapper recordMapper;
    private final ApprovalChainMapper chainMapper;
    private final PlanMapper planMapper;
    private final AchievementMapper achievementMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    /**
     * Get pending approvals for current user.
     */
    public List<ApprovalRecord> getPendingApprovals() {
        Long userId = UserContext.getUserId();
        return recordMapper.selectList(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getApproverId, userId)
                .isNull(ApprovalRecord::getAction)
                .orderByDesc(ApprovalRecord::getCreatedAt));
    }

    /**
     * Start approval workflow for a plan or achievement.
     */
    @Transactional
    public void startApproval(Long targetId, String targetType, Long submitterId) {
        User submitter = userMapper.selectById(submitterId);
        if (submitter == null) return;

        Long deptId = submitter.getDeptId();
        String planType = "ACHIEVEMENT".equals(targetType) ? "ACHIEVEMENT" : getTargetPlanType(targetId);

        // Get approval chain config
        List<ApprovalChain> chains = chainMapper.selectList(new LambdaQueryWrapper<ApprovalChain>()
                .eq(ApprovalChain::getDeptId, deptId)
                .eq(ApprovalChain::getPlanType, planType)
                .orderByAsc(ApprovalChain::getApprovalLevel, ApprovalChain::getSortOrder));

        if (chains.isEmpty()) {
            // Fallback: leader(level=1) -> dept head(level=2)
            createDefaultApprovalRecords(targetId, targetType, submitterId);
        } else {
            for (ApprovalChain chain : chains) {
                ApprovalRecord record = new ApprovalRecord();
                record.setTargetId(targetId);
                record.setTargetType(targetType);
                record.setApproverId(chain.getApproverId());
                record.setApprovalLevel(chain.getApprovalLevel());
                record.setSortOrder(chain.getSortOrder());
                recordMapper.insert(record);

                notificationService.createNotification(
                        chain.getApproverId(),
                        "NEW_APPROVAL_" + ("ACHIEVEMENT".equals(targetType) ? "ACHV" : "PLAN"),
                        "有新的待审批" + ("ACHIEVEMENT".equals(targetType) ? "成果" : "计划"),
                        "提交了一份计划，请审批",
                        targetId, targetType);
            }
        }
    }

    private void createDefaultApprovalRecords(Long targetId, String targetType, Long submitterId) {
        User submitter = userMapper.selectById(submitterId);
        User leader = submitter.getLeaderId() != null ? userMapper.selectById(submitter.getLeaderId()) : null;

        if (leader != null) {
            ApprovalRecord r = new ApprovalRecord();
            r.setTargetId(targetId);
            r.setTargetType(targetType);
            r.setApproverId(leader.getId());
            r.setApprovalLevel(1);
            r.setSortOrder(0);
            recordMapper.insert(r);
            notificationService.createNotification(leader.getId(), "NEW_APPROVAL_PLAN",
                    "新的待审批计划", "有新的计划待审批", targetId, targetType);
        }
    }

    @Transactional
    public void approve(Long recordId, String comment) {
        processApproval(recordId, "APPROVE", comment, null);
    }

    @Transactional
    public void reject(Long recordId, String comment) {
        processApproval(recordId, "REJECT", comment, null);
    }

    @Transactional
    public void transfer(Long recordId, Long targetUserId, String comment) {
        User targetUser = userMapper.selectById(targetUserId);
        User currentUser = userMapper.selectById(UserContext.getUserId());
        if (!currentUser.getDeptId().equals(targetUser.getDeptId())) {
            throw new BusinessException(ErrorCode.TRANSFER_TARGET_NOT_IN_SAME_DEPT);
        }
        processApproval(recordId, "TRANSFER", comment, targetUserId);
    }

    private void processApproval(Long recordId, String action, String comment, Long transferTargetId) {
        ApprovalRecord record = recordMapper.selectById(recordId);
        if (record == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        if (!record.getApproverId().equals(UserContext.getUserId())) throw new BusinessException(ErrorCode.NOT_APPROVER);
        if (record.getAction() != null) throw new BusinessException(ErrorCode.APPROVAL_ALREADY_PROCESSED);

        record.setAction(action);
        record.setComment(comment);
        record.setApprovedAt(LocalDateTime.now());
        recordMapper.updateById(record);

        if ("REJECT".equals(action)) {
            updateTargetStatus(record.getTargetId(), record.getTargetType(), "REJECTED");
            // Notify submitter
            notifySubmitter(record, "被驳回");
        } else if ("TRANSFER".equals(action) && transferTargetId != null) {
            ApprovalRecord newRecord = new ApprovalRecord();
            newRecord.setTargetId(record.getTargetId());
            newRecord.setTargetType(record.getTargetType());
            newRecord.setApproverId(transferTargetId);
            newRecord.setApprovalLevel(record.getApprovalLevel());
            newRecord.setSortOrder(record.getSortOrder() + 1);
            recordMapper.insert(newRecord);
            notificationService.createNotification(transferTargetId, "NEW_APPROVAL_PLAN",
                    "转审通知", "审批已转给您处理", record.getTargetId(), record.getTargetType());
        } else if ("APPROVE".equals(action)) {
            // Check if same-level all approved
            boolean allApproved = isLevelFullyApproved(record.getTargetId(), record.getTargetType(), record.getApprovalLevel());
            if (allApproved) {
                // Check if last level
                boolean isLast = isLastLevel(record.getTargetId(), record.getTargetType(), record.getApprovalLevel());
                if (isLast) {
                    updateTargetStatus(record.getTargetId(), record.getTargetType(), "APPROVED");
                    notifySubmitter(record, "已通过");
                }
            }
        }
    }

    private void updateTargetStatus(Long targetId, String targetType, String status) {
        if ("PLAN".equals(targetType)) {
            Plan p = planMapper.selectById(targetId);
            if (p != null) { p.setStatus(status); planMapper.updateById(p); }
        } else {
            Achievement a = achievementMapper.selectById(targetId);
            if (a != null) { a.setStatus(status); achievementMapper.updateById(a); }
        }
    }

    private boolean isLevelFullyApproved(Long targetId, String targetType, int level) {
        return recordMapper.selectCount(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getTargetId, targetId)
                .eq(ApprovalRecord::getTargetType, targetType)
                .eq(ApprovalRecord::getApprovalLevel, level)
                .ne(ApprovalRecord::getAction, "APPROVE")) == 0;
    }

    private boolean isLastLevel(Long targetId, String targetType, int currentLevel) {
        return recordMapper.selectCount(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getTargetId, targetId)
                .eq(ApprovalRecord::getTargetType, targetType)
                .gt(ApprovalRecord::getApprovalLevel, currentLevel)) == 0;
    }

    private void notifySubmitter(ApprovalRecord record, String actionDesc) {
        Long submitterId = getSubmitterId(record.getTargetId(), record.getTargetType());
        if (submitterId != null) {
            notificationService.createNotification(submitterId,
                    "REJECTED".equals(record.getAction()) ? "PLAN_REJECTED" : "PLAN_APPROVED",
                    "审批结果通知", "您的计划已被" + actionDesc, record.getTargetId(), record.getTargetType());
        }
    }

    private Long getSubmitterId(Long targetId, String targetType) {
        if ("PLAN".equals(targetType)) {
            Plan p = planMapper.selectById(targetId);
            return p != null ? p.getUserId() : null;
        }
        return null;
    }

    private String getTargetPlanType(Long targetId) {
        Plan p = planMapper.selectById(targetId);
        return p != null ? p.getPlanType() : "DAILY";
    }
}
