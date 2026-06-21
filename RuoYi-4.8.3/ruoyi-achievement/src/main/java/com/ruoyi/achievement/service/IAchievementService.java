package com.ruoyi.achievement.service;

import java.util.List;
import com.ruoyi.achievement.domain.Achievement;

public interface IAchievementService {
    List<Achievement> selectAchievementList(Achievement achievement);
    Achievement selectAchievementById(Long id);
    int insertAchievement(Achievement achievement);
    int updateAchievement(Achievement achievement);
    void submitAchievement(Long achievementId);
    void withdrawAchievement(Long achievementId);
    void deleteAchievement(Long achievementId);
    String getComparisonStatus(Long achievementId);
}
