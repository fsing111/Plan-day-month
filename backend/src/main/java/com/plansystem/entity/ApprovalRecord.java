package com.plansystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("approval_record")
public class ApprovalRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long targetId;
    private String targetType;
    private Long approverId;
    private Integer approvalLevel;
    private String action;
    private String comment;
    private LocalDateTime approvedAt;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
