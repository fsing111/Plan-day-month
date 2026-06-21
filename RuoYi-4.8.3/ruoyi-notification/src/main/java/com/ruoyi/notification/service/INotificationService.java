package com.ruoyi.notification.service;

import java.util.List;
import com.ruoyi.notification.domain.Notification;

public interface INotificationService {
    List<Notification> selectNotificationList(Long receiverId);
    int getUnreadCount(Long receiverId);
    void createNotification(Notification notification);
    void markAsRead(Long id);
    void markAllAsRead(Long receiverId);
    void deleteById(Long id);
}
