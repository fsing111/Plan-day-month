package com.plansystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plansystem.dto.ApprovalVO;
import com.plansystem.dto.PageResult;
import com.plansystem.entity.*;
import com.plansystem.exception.BusinessException;
import com.plansystem.exception.ErrorCode;
import com.plansystem.mapper.*;
import com.plansystem.utils.DateUtils;
import com.plansystem.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
     * Get pending approvals for current user, optionally filtered by target type.
     * Returns enriched ApprovalVO with target details and submitter info.
     */
    public List<ApprovalVO> getPendingApprovals(String targetType) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<ApprovalRecord> wrapper = new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getApproverId, userId)
                .isNull(ApprovalRecord::getAction);
        if (targetType != null && !targetType.isBlank()) {
            wrapper.eq(ApprovalRecord::getTargetType, targetType);
        }
        wrapper.orderByDesc(ApprovalRecord::getCreatedAt);
        List<ApprovalRecord> records = recordMapper.selectList(wrapper);

        return enrichApprovalRecords(records);
    }

    /**
     * Get approval timeline for a specific target (plan or achievement).
     */
    public List<ApprovalRecord> getApprovalTimeline(String targetType, Long targetId) {
        return recordMapper.selectList(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getTargetId, targetId)
                .eq(ApprovalRecord::getTargetType, targetType)
                .orderByAsc(ApprovalRecord::getApprovalLevel, ApprovalRecord::getSortOrder));
    }

    /**
     * Get approval history for current user with pagination.
     * Returns enriched ApprovalVO with target details and submitter info.
     */
    public PageResult<ApprovalVO> getApprovalHistory(int page, int pageSize, String targetType, String action) {
        Long userId = UserContext.getUserId();
        IPage<ApprovalRecord> p = new Page<>(page, Math.min(pageSize, 100));
        LambdaQueryWrapper<ApprovalRecord> w = new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getApproverId, userId)
                .isNotNull(ApprovalRecord::getAction);
        if (targetType != null && !targetType.isBlank()) {
            w.eq(ApprovalRecord::getTargetType, targetType);
        }
        if (action != null && !action.isBlank()) {
            w.eq(ApprovalRecord::getAction, action);
        }
        w.orderByDesc(ApprovalRecord::getApprovedAt);
        IPage<ApprovalRecord> resultPage = recordMapper.selectPage(p, w);

        List<ApprovalVO> enriched = enrichApprovalRecords(resultPage.getRecords());
        return PageResult.of(enriched, resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
    }

    /**
     * Start approval workflow for a plan or achievement.
     */
    @Transactional
    public void startApproval(Long targetId, String targetType, Long submitterId) {
        User submitter = userMapper.selectById(submitterId);
        if (submitter == null) return;

        Long deptId = submitter.getDeptId();
        String chainPlanType = "ACHIEVEMENT".equals(targetType) ? "ACHIEVEMENT" : getTargetPlanType(targetId, targetType);

        // Get approval chain config
        List<ApprovalChain> chains = chainMapper.selectList(new LambdaQueryWrapper<ApprovalChain>()
                .eq(ApprovalChain::getDeptId, deptId)
                .eq(ApprovalChain::getPlanType, chainPlanType)
                .orderByAsc(ApprovalChain::getApprovalLevel, ApprovalChain::getSortOrder));

        if (chains.isEmpty()) {
            // Fallback: leader(level=1)
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

        // Update target status to APPROVING
        updateTargetStatus(targetId, targetType, "APPROVING");
    }

    /**
     * Batch approve multiple approval records at once.
     */
    @Transactional
    public Map<String, Object> batchApprove(List<Long> recordIds, String comment) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;

        for (Long recordId : recordIds) {
            try {
                approve(recordId, comment);
                successCount++;
            } catch (Exception e) {
                errors.add("记录 " + recordId + ": " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("errorCount", errors.size());
        result.put("errors", errors);
        return result;
    }

    /**
     * Enrich a list of ApprovalRecord with target details, submitter info,
     * peer approvals, and total levels.
     */
    private List<ApprovalVO> enrichApprovalRecords(List<ApprovalRecord> records) {
        if (records.isEmpty()) return List.of();

        // Collect all needed IDs
        Set<Long> userIds = new HashSet<>();
        Map<String, List<Long>> targetIdsByType = new HashMap<>();
        targetIdsByType.put("PLAN", new ArrayList<>());
        targetIdsByType.put("ACHIEVEMENT", new ArrayList<>());

        for (ApprovalRecord r : records) {
            userIds.add(r.getApproverId());
            targetIdsByType.computeIfAbsent(r.getTargetType(), k -> new ArrayList<>()).add(r.getTargetId());
        }

        // Batch load users, plans, achievements
        Map<Long, User> userMap = loadUserMap(userIds);
        Map<Long, Plan> planMap = loadPlanMap(targetIdsByType.get("PLAN"));
        Map<Long, Achievement> achievementMap = loadAchievementMap(targetIdsByType.get("ACHIEVEMENT"));

        // Build submitterId -> submitterName map (plan.userId / achievement→plan.userId)
        Map<Long, Long> targetSubmitterMap = new HashMap<>();
        Set<Long> submitterIds = new HashSet<>();
        for (Plan p : planMap.values()) {
            targetSubmitterMap.put(p.getId(), p.getUserId());
            submitterIds.add(p.getUserId());
        }
        for (Achievement a : achievementMap.values()) {
            Plan relatedPlan = planMapper.selectById(a.getPlanId());
            Long sid = relatedPlan != null ? relatedPlan.getUserId() : null;
            if (sid != null) {
                targetSubmitterMap.put(a.getId(), sid);
                submitterIds.add(sid);
            }
        }
        Map<Long, User> submitterUserMap = loadUserMap(submitterIds);

        // Build enriched VOs
        return records.stream().map(r -> {
            ApprovalVO.ApprovalVOBuilder builder = ApprovalVO.builder()
                    .id(r.getId())
                    .targetId(r.getTargetId())
                    .targetType(r.getTargetType())
                    .approverId(r.getApproverId())
                    .approvalLevel(r.getApprovalLevel())
                    .action(r.getAction())
                    .comment(r.getComment())
                    .approvedAt(DateUtils.format(r.getApprovedAt()))
                    .sortOrder(r.getSortOrder())
                    .createdAt(DateUtils.format(r.getCreatedAt()));

            // Approver name
            User approver = userMap.get(r.getApproverId());
            // approver name not needed in list view, but kept for completeness

            // Total levels
            int totalLevels = getTotalLevels(r.getTargetId(), r.getTargetType());
            builder.totalLevels(totalLevels);

            // Target info (plan or achievement)
            if ("PLAN".equals(r.getTargetType())) {
                Plan plan = planMap.get(r.getTargetId());
                if (plan != null) {
                    builder.title(plan.getTitle())
                            .planType(plan.getPlanType())
                            .priority(plan.getPriority())
                            .submittedAt(DateUtils.format(plan.getCreatedAt()));
                }
            } else {
                Achievement achv = achievementMap.get(r.getTargetId());
                if (achv != null) {
                    Plan plan = planMapper.selectById(achv.getPlanId());
                    builder.title(plan != null ? plan.getTitle() : achv.getDescription())
                            .planType(plan != null ? plan.getPlanType() : "")
                            .priority(plan != null ? plan.getPriority() : "")
                            .submittedAt(DateUtils.format(achv.getSubmittedAt()));
                }
            }

            // Submitter info
            Long submitterId = targetSubmitterMap.get(r.getTargetId());
            if (submitterId != null) {
                builder.submitterId(submitterId);
                User submitter = submitterUserMap.get(submitterId);
                builder.submitterName(submitter != null ? submitter.getRealName() : "未知");
            } else {
                builder.submitterName("未知");
            }

            // Peer approvals at the same level (exclude self)
            List<ApprovalRecord> peers = getPeerApprovals(r.getTargetId(), r.getTargetType(), r.getApprovalLevel(), r.getId());
            List<ApprovalVO.PeerApproval> peerVOs = peers.stream().map(peer -> {
                User peerUser = userMap.get(peer.getApproverId());
                return ApprovalVO.PeerApproval.builder()
                        .approverName(peerUser != null ? peerUser.getRealName() : "未知")
                        .action(peer.getAction())
                        .comment(peer.getComment())
                        .approvedAt(DateUtils.format(peer.getApprovedAt()))
                        .build();
            }).collect(Collectors.toList());
            builder.peerApprovals(peerVOs);

            return builder.build();
        }).collect(Collectors.toList());
    }

    private Map<Long, User> loadUserMap(Set<Long> userIds) {
        if (userIds.isEmpty()) return Map.of();
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
    }

    private Map<Long, Plan> loadPlanMap(List<Long> planIds) {
        if (planIds == null || planIds.isEmpty()) return Map.of();
        List<Long> distinct = planIds.stream().distinct().toList();
        return planMapper.selectBatchIds(distinct).stream()
                .collect(Collectors.toMap(Plan::getId, p -> p));
    }

    private Map<Long, Achievement> loadAchievementMap(List<Long> achievementIds) {
        if (achievementIds == null || achievementIds.isEmpty()) return Map.of();
        List<Long> distinct = achievementIds.stream().distinct().toList();
        return achievementMapper.selectBatchIds(distinct).stream()
                .collect(Collectors.toMap(Achievement::getId, a -> a));
    }

    private int getTotalLevels(Long targetId, String targetType) {
        List<Object> distinctLevels = recordMapper.selectObjs(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getTargetId, targetId)
                .eq(ApprovalRecord::getTargetType, targetType)
                .select(ApprovalRecord::getApprovalLevel)
                .groupBy(ApprovalRecord::getApprovalLevel));
        return distinctLevels.size();
    }

    private List<ApprovalRecord> getPeerApprovals(Long targetId, String targetType, int level, Long excludeRecordId) {
        return recordMapper.selectList(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getTargetId, targetId)
                .eq(ApprovalRecord::getTargetType, targetType)
                .eq(ApprovalRecord::getApprovalLevel, level)
                .ne(ApprovalRecord::getId, excludeRecordId));
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
        if (comment == null || comment.isBlank()) {
            throw new BusinessException(ErrorCode.APPROVAL_REJECT_COMMENT_REQUIRED);
        }
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
                boolean isLast = isLastLevel(record.getTargetId(), record.getTargetType(), record.getApprovalLevel());
                if (isLast) {
                    updateTargetStatus(record.getTargetId(), record.getTargetType(), "APPROVED");
                }
                notifySubmitter(record, "已通过");
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
            String notificationType = "REJECTED".equals(record.getAction()) ? "PLAN_REJECTED" : "PLAN_APPROVED";
            String targetLabel = "ACHIEVEMENT".equals(record.getTargetType()) ? "成果" : "计划";
            notificationService.createNotification(submitterId,
                    notificationType,
                    "审批结果通知",
                    "您的" + targetLabel + "已被" + actionDesc,
                    record.getTargetId(), record.getTargetType());
        }
    }

    private Long getSubmitterId(Long targetId, String targetType) {
        if ("PLAN".equals(targetType)) {
            Plan p = planMapper.selectById(targetId);
            return p != null ? p.getUserId() : null;
        } else if ("ACHIEVEMENT".equals(targetType)) {
            Achievement a = achievementMapper.selectById(targetId);
            if (a != null) {
                Plan p = planMapper.selectById(a.getPlanId());
                return p != null ? p.getUserId() : null;
            }
        }
        return null;
    }

    private String getTargetPlanType(Long targetId, String targetType) {
        if ("ACHIEVEMENT".equals(targetType)) return "ACHIEVEMENT";
        Plan p = planMapper.selectById(targetId);
        return p != null ? p.getPlanType() : "DAILY";
    }
}
