package com.plansystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Plan revision entity for tracking changes between submissions.
 */
@Data
@TableName("plan_revision")
public class PlanRevision {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long planId;
    private Integer version;
    private String changes;
    private String submitterNote;
    private LocalDateTime createdAt;
}
