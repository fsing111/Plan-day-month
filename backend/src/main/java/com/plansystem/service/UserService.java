package com.plansystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plansystem.entity.User;
import com.plansystem.exception.BusinessException;
import com.plansystem.exception.ErrorCode;
import com.plansystem.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User service with data permission support.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    /**
     * Find user by ID.
     */
    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    /**
     * Get all subordinate user IDs (recursive, including all levels below).
     */
    public List<Long> getAllSubordinateIds(Long leaderId) {
        List<Long> result = new ArrayList<>();
        collectSubordinatesRecursive(leaderId, result);
        return result;
    }

    private void collectSubordinatesRecursive(Long leaderId, List<Long> result) {
        List<User> directSubordinates = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .eq(User::getLeaderId, leaderId)
                        .eq(User::getEnabled, 1)
        );

        for (User subordinate : directSubordinates) {
            result.add(subordinate.getId());
            // Recursively collect subordinates of this leader
            collectSubordinatesRecursive(subordinate.getId(), result);
        }
    }

    /**
     * Get the IDs of users visible to the current user based on role.
     */
    public List<Long> getVisibleUserIds(Long currentUserId, String currentRole) {
        if ("ADMIN".equals(currentRole)) {
            // Admin sees all
            return null; // null means no filtering
        } else if ("LEADER".equals(currentRole)) {
            // Leader sees self + all subordinates
            List<Long> ids = getAllSubordinateIds(currentUserId);
            ids.add(currentUserId);
            return ids;
        } else {
            // Employee sees only self
            return List.of(currentUserId);
        }
    }

    /**
     * Find all users by IDs.
     */
    public List<User> getByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return userMapper.selectBatchIds(ids);
    }

    /**
     * Get user name map (id -> realName).
     */
    public java.util.Map<Long, String> getUserNameMap(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Map.of();
        }
        List<User> users = getByIds(ids);
        return users.stream()
                .collect(Collectors.toMap(User::getId, User::getRealName));
    }
}
