package com.example.shitang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.shitang.common.BizException;
import com.example.shitang.dto.CommentRequest;
import com.example.shitang.entity.Comment;
import com.example.shitang.entity.Dish;
import com.example.shitang.entity.User;
import com.example.shitang.mapper.CommentMapper;
import com.example.shitang.service.BehaviorService;
import com.example.shitang.utils.SanitizeUtils;
import com.example.shitang.service.CommentService;
import com.example.shitang.service.DishService;
import com.example.shitang.service.NotificationService;
import com.example.shitang.service.UserService;
import com.example.shitang.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    private final UserService userService;
    private final DishService dishService;
    private final BehaviorService behaviorService;
    private final NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO createComment(CommentRequest request) {
        User user = userService.currentUserEntity();
        Dish dish = requireAvailableDish(request.getDishId());

        Comment comment = new Comment();
        comment.setUserId(user.getId());
        comment.setDishId(request.getDishId());
        comment.setContent(SanitizeUtils.sanitizeHtml(SanitizeUtils.cleanText(request.getContent())));
        comment.setRating(request.getRating());
        comment.setStatus(1);
        comment.setCreateTime(LocalDateTime.now());
        save(comment);

        behaviorService.record(user.getId(), request.getDishId(), "COMMENT", BigDecimal.valueOf(3.50));
        behaviorService.record(user.getId(), request.getDishId(), "RATE", request.getRating());
        recalculateAvgRating(dish);
        return toVO(comment);
    }

    @Override
    public List<CommentVO> listByDish(Long dishId) {
        return list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getDishId, dishId)
                .eq(Comment::getStatus, 1)
                .orderByDesc(Comment::getCreateTime))
                .stream().map(this::toVO).toList();
    }

    @Override
    public List<CommentVO> listMine() {
        Long userId = userService.currentUserEntity().getId();
        return list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getUserId, userId)
                .orderByDesc(Comment::getCreateTime))
                .stream().map(this::toVO).toList();
    }

    @Override
    public List<CommentVO> listAll(String keyword, Integer status) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .eq(status != null, Comment::getStatus, status)
                .orderByDesc(Comment::getCreateTime);
        return list(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    public CommentVO replyToComment(Long commentId, String reply) {
        Comment comment = getById(commentId);
        if (comment == null) throw new BizException("评论不存在");
        comment.setReply(SanitizeUtils.sanitizeHtml(SanitizeUtils.cleanText(reply)));
        updateById(comment);
        // 通知评论作者
        Dish dish = dishService.getById(comment.getDishId());
        String dishName = dish != null ? dish.getName() : "菜品";
        notificationService.send(comment.getUserId(), "COMMENT_REPLY",
                "商家回复了你的评论", "你在「" + dishName + "」的评论收到了商家回复", commentId);
        return toVO(comment);
    }

    private void recalculateAvgRating(Dish dish) {
        List<Comment> comments = list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getDishId, dish.getId())
                .eq(Comment::getStatus, 1));
        if (comments.isEmpty()) {
            dish.setAvgRating(BigDecimal.ZERO);
        } else {
            BigDecimal sum = comments.stream()
                    .map(Comment::getRating)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dish.setAvgRating(sum.divide(BigDecimal.valueOf(comments.size()), 2, RoundingMode.HALF_UP));
        }
        dish.setUpdateTime(LocalDateTime.now());
        dishService.updateById(dish);
    }

    private CommentVO toVO(Comment comment) {
        CommentVO vo = CommentVO.from(comment);
        User user = userService.getById(comment.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
        }
        Dish dish = dishService.getById(comment.getDishId());
        if (dish != null) {
            vo.setDishName(dish.getName());
        }
        return vo;
    }

    private Dish requireAvailableDish(Long dishId) {
        Dish dish = dishService.getById(dishId);
        if (dish == null) {
            throw new BizException("菜品不存在");
        }
        if (dish.getStatus() == null || dish.getStatus() != 1) {
            throw new BizException("菜品未上架");
        }
        return dish;
    }
}
