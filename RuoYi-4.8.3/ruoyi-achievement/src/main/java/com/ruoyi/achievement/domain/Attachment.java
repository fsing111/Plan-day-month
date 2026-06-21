package com.ruoyi.achievement.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class Attachment extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long achievementId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAchievementId() { return achievementId; }
    public void setAchievementId(Long achievementId) { this.achievementId = achievementId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
}
