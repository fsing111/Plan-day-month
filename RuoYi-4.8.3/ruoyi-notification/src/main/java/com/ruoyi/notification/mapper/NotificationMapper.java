package com.ruoyi.notification.mapper;

import java.util.List;
import com.ruoyi.notification.domain.Notification;

public interface NotificationMapper {
    List<Notification> selectNotificationList(Notification notification);
    int selectUnreadCount(Long receiverId);
    int insertNotification(Notification notification);
    int markAsRead(Long id);
    int markAllAsRead(Long receiverId);
    int deleteById(Long id);
}
