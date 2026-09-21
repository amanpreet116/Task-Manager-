package com.taskmanager.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Stable JSON contract shared by task-event producers and consumers.
 */
public record TaskEventDto(
        String eventType,
        UUID taskId,
        String title,
        String status,
        Instant timestamp
) {
}
