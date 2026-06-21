package com.plansystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plansystem.entity.PlanComment;
import com.plansystem.exception.BusinessException;
import com.plansystem.exception.ErrorCode;
import com.plansystem.mapper.PlanCommentMapper;
import com.plansystem.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Comment service for plan and achievement discussions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final PlanCommentMapper commentMapper;

    public List<PlanComment> getComments(Long targetId, String targetType) {
        return commentMapper.selectList(new LambdaQueryWrapper<PlanComment>()
                .eq(PlanComment::getTargetId, targetId)
                .eq(PlanComment::getTargetType, targetType)
                .orderByAsc(PlanComment::getCreatedAt));
    }

    @Transactional
    public PlanComment addComment(Long targetId, String targetType, String content) {
        PlanComment comment = new PlanComment();
        comment.setTargetId(targetId);
        comment.setTargetType(targetType);
        comment.setUserId(UserContext.getUserId());
        comment.setContent(content);
        commentMapper.insert(comment);
        log.info("Comment added: targetId={}, targetType={}, userId={}", targetId, targetType, comment.getUserId());
        return comment;
    }

    @Transactional
    public void deleteComment(Long id) {
        PlanComment c = commentMapper.selectById(id);
        if (c == null) throw new BusinessException(ErrorCode.NOT_FOUND, "评论不存在");
        commentMapper.deleteById(id);
    }
}
