package com.ruoyi.achievement.mapper;

import java.util.List;
import java.util.Map;

import com.ruoyi.achievement.domain.Achievement;

public interface AchievementMapper {
    List<Achievement> selectAchievementList(Achievement achievement);
    Achievement selectAchievementById(Long id);
    int insertAchievement(Achievement achievement);
    int updateAchievement(Achievement achievement);
    int updateAchievementStatus(Map<String, Object> params);
    int deleteAchievementById(Long id);
}
