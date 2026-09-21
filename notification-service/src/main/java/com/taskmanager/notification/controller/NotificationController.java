package com.taskmanager.notification.controller;

import com.taskmanager.notification.dto.NotificationResponseDto;
import com.taskmanager.notification.service.NotificationQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationQueryService notificationQueryService;

    public NotificationController(
            NotificationQueryService notificationQueryService) {
        this.notificationQueryService = notificationQueryService;
    }

    @GetMapping
    public Page<NotificationResponseDto> getRecentNotifications(
            @PageableDefault(size = 20) Pageable pageable) {
        return notificationQueryService.getRecentNotifications(pageable);
    }
}
