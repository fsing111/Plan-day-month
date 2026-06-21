package com.ruoyi.achievement.mapper;

import java.util.List;

import com.ruoyi.achievement.domain.Attachment;

public interface AttachmentMapper {
    List<Attachment> selectByAchievementId(Long achievementId);
    Attachment selectById(Long id);
    int insertAttachment(Attachment attachment);
    int deleteById(Long id);
}
