package com.taskmanager.notification.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Local copy of the task-events JSON contract published by task-manager.
 */
public record TaskEventDto(
        String eventType,
        UUID taskId,
        String title,
        String status,
        Instant timestamp
) {
}
