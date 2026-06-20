package com.plansystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("approval_chain")
public class ApprovalChain {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deptId;
    private String planType;
    private Integer approvalLevel;
    private Long approverId;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
