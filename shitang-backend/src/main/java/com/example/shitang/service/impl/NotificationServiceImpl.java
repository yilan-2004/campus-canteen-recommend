package com.example.shitang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.shitang.entity.Notification;
import com.example.shitang.mapper.NotificationMapper;
import com.example.shitang.service.NotificationService;
import com.example.shitang.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
    private final UserService userService;

    @Override
    public void send(Long userId, String type, String title, String content, Long relatedId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setRelatedId(relatedId);
        n.setIsRead(0);
        n.setCreateTime(LocalDateTime.now());
        save(n);
    }

    @Override
    public List<Notification> myNotifications(boolean unreadOnly) {
        Long userId = userService.currentUserEntity().getId();
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(unreadOnly, Notification::getIsRead, 0)
                .orderByDesc(Notification::getCreateTime);
        return list(wrapper);
    }

    @Override
    public int unreadCount() {
        Long userId = userService.currentUserEntity().getId();
        return (int) count(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
    }

    @Override
    public void markRead(Long id) {
        Notification n = getById(id);
        if (n != null) {
            n.setIsRead(1);
            updateById(n);
        }
    }

    @Override
    public void markAllRead() {
        Long userId = userService.currentUserEntity().getId();
        List<Notification> unread = list(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
        for (Notification n : unread) {
            n.setIsRead(1);
        }
        updateBatchById(unread);
    }
}
