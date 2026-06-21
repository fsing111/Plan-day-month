package com.ruoyi.notification.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.notification.domain.Notification;
import com.ruoyi.notification.mapper.NotificationMapper;
import com.ruoyi.notification.service.INotificationService;

@Service
public class NotificationServiceImpl implements INotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public List<Notification> selectNotificationList(Long receiverId) {
        Notification query = new Notification();
        query.setReceiverId(receiverId);
        return notificationMapper.selectNotificationList(query);
    }

    @Override
    public int getUnreadCount(Long receiverId) {
        return notificationMapper.selectUnreadCount(receiverId);
    }

    @Override
    public void createNotification(Notification notification) {
        notificationMapper.insertNotification(notification);
    }

    @Override
    public void markAsRead(Long id) {
        notificationMapper.markAsRead(id);
    }

    @Override
    public void markAllAsRead(Long receiverId) {
        notificationMapper.markAllAsRead(receiverId);
    }

    @Override
    public void deleteById(Long id) {
        notificationMapper.deleteById(id);
    }
}
