package com.taskmanager.notification.dto;

import com.taskmanager.notification.entity.NotificationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponseDto(
        UUID id,
        UUID taskId,
        String message,
        NotificationStatus status,
        LocalDateTime createdAt
) {
}
