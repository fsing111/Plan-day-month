package com.plansystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Plan entity.
 */
@Data
@TableName("plan")
public class Plan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String planType;
    private String title;
    private String description;
    private String priority;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long categoryId;
    private String quantTarget;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
