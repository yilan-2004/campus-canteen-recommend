package com.example.shitang.controller;

import com.example.shitang.common.Result;
import com.example.shitang.entity.Notification;
import com.example.shitang.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public Result<List<Notification>> list(@RequestParam(defaultValue = "false") boolean unreadOnly) {
        return Result.success(notificationService.myNotifications(unreadOnly));
    }

    @GetMapping("/unread-count")
    public Result<Integer> unreadCount() {
        return Result.success(notificationService.unreadCount());
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        notificationService.markAllRead();
        return Result.success();
    }
}
