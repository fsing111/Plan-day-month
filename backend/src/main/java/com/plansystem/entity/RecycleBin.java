package com.plansystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Recycle bin entity for soft-deleted records.
 */
@Data
@TableName("recycle_bin")
public class RecycleBin {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String originalTable;
    private Long originalId;
    private String title;
    private String deleteReason;
    private Long deletedBy;
    private LocalDateTime deletedAt;
    private LocalDateTime canRestoreUntil;
}
