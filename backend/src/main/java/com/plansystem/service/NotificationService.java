package com.plansystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plansystem.dto.PageResult;
import com.plansystem.entity.Notification;
import com.plansystem.mapper.NotificationMapper;
import com.plansystem.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;

    public PageResult<Notification> getNotifications(int page, int pageSize) {
        Long userId = UserContext.getUserId();
        IPage<Notification> pageParam = new Page<>(page, Math.min(pageSize, 100));
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverId, userId)
                .orderByDesc(Notification::getCreatedAt);

        IPage<Notification> result = notificationMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result);
    }

    public long getUnreadCount() {
        Long userId = UserContext.getUserId();
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverId, userId)
                .eq(Notification::getIsRead, 0));
    }

    @Transactional
    public void markAsRead(Long id) {
        Notification n = notificationMapper.selectById(id);
        if (n != null && n.getReceiverId().equals(UserContext.getUserId())) {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        }
    }

    @Transactional
    public void markAllAsRead() {
        notificationMapper.markAllAsRead(UserContext.getUserId());
    }

    @Transactional
    public void deleteNotification(Long id) {
        Notification n = notificationMapper.selectById(id);
        if (n != null && n.getReceiverId().equals(UserContext.getUserId())) {
            notificationMapper.deleteById(id);
        }
    }

    @Transactional
    public void createNotification(Long receiverId, String type, String title, String content,
                                    Long relatedId, String relatedType) {
        Notification notification = new Notification();
        notification.setReceiverId(receiverId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(0);
        notification.setRelatedId(relatedId);
        notification.setRelatedType(relatedType);
        notificationMapper.insert(notification);
        log.info("Notification created: receiverId={}, type={}, title={}", receiverId, type, title);
    }
}
