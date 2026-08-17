package com.taskmanager.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AttachmentResponseDto(
        UUID id,
        String fileName,
        String contentType,
        UUID taskId,
        LocalDateTime uploadedAt,
        String downloadUrl
) {
}
