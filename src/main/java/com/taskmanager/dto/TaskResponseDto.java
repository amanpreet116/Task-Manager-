package com.taskmanager.dto;

import com.taskmanager.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class TaskResponseDto {

    private final UUID id;
    private final String title;
    private final String description;
    private final TaskStatus status;
    private final UUID ownerId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<SubtaskResponseDto> subtasks;
}
