package com.example.shitang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.shitang.entity.Notification;

import java.util.List;

public interface NotificationService extends IService<Notification> {
    void send(Long userId, String type, String title, String content, Long relatedId);
    List<Notification> myNotifications(boolean unreadOnly);
    int unreadCount();
    void markRead(Long id);
    void markAllRead();
}
