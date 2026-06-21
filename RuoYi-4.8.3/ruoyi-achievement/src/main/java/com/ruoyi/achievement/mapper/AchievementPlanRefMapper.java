package com.ruoyi.achievement.mapper;

import java.util.List;

import com.ruoyi.achievement.domain.AchievementPlanRef;

public interface AchievementPlanRefMapper {
    int batchInsert(List<AchievementPlanRef> refs);
    List<AchievementPlanRef> selectByAchievementId(Long achievementId);
    int deleteByAchievementId(Long achievementId);
}
