package com.ruoyi.plan.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 回收站对象 recycle_bin
 *
 * @author ruoyi
 */
public class RecycleBin extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String originalTable;
    private Long originalId;
    private String title;
    private String deleteReason;
    private Long deletedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date canRestoreUntil;

    /** 非数据库字段 */
    private String deletedByName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOriginalTable() { return originalTable; }
    public void setOriginalTable(String originalTable) { this.originalTable = originalTable; }

    public Long getOriginalId() { return originalId; }
    public void setOriginalId(Long originalId) { this.originalId = originalId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDeleteReason() { return deleteReason; }
    public void setDeleteReason(String deleteReason) { this.deleteReason = deleteReason; }

    public Long getDeletedBy() { return deletedBy; }
    public void setDeletedBy(Long deletedBy) { this.deletedBy = deletedBy; }

    public Date getCanRestoreUntil() { return canRestoreUntil; }
    public void setCanRestoreUntil(Date canRestoreUntil) { this.canRestoreUntil = canRestoreUntil; }

    public String getDeletedByName() { return deletedByName; }
    public void setDeletedByName(String deletedByName) { this.deletedByName = deletedByName; }
}
