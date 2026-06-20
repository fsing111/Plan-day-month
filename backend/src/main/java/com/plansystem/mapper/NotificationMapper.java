package com.plansystem.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plansystem.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
    @Update("UPDATE notification SET is_read = 1 WHERE receiver_id = #{userId}")
    int markAllAsRead(Long userId);
}
