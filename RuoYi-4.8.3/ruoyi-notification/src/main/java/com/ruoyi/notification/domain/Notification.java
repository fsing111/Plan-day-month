package com.ruoyi.notification.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class Notification extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long receiverId;
    private String type;
    private String title;
    private String content;
    private Integer isRead;
    private Long relatedId;
    private String relatedType;

    // 非数据库字段
    private String receiverName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getIsRead() { return isRead; }
    public void setIsRead(Integer isRead) { this.isRead = isRead; }
    public Long getRelatedId() { return relatedId; }
    public void setRelatedId(Long relatedId) { this.relatedId = relatedId; }
    public String getRelatedType() { return relatedType; }
    public void setRelatedType(String relatedType) { this.relatedType = relatedType; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
}
