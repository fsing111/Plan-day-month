package com.plansystem.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Approval view object returned to frontend — enriched with target details,
 * submitter info, and peer approval status.
 */
@Data
@Builder
public class ApprovalVO {

    // --- from approval_record ---
    private Long id;
    private Long targetId;
    private String targetType;
    private Long approverId;
    private Integer approvalLevel;
    private Integer totalLevels;
    private String action;
    private String comment;
    private String approvedAt;
    private Integer sortOrder;
    private String createdAt;

    // --- submitter info ---
    private Long submitterId;
    private String submitterName;

    // --- target info (plan or achievement) ---
    private String title;
    private String planType;
    private String priority;
    private String submittedAt;

    // --- same-level peer approvals ---
    private List<PeerApproval> peerApprovals;

    @Data
    @Builder
    public static class PeerApproval {
        private String approverName;
        private String action;
        private String comment;
        private String approvedAt;
    }
}
