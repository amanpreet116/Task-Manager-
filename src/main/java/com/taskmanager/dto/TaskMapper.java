package com.taskmanager.dto;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;

import java.util.Objects;

public final class TaskMapper {

    private TaskMapper() {
    }

    public static Task toEntity(TaskRequestDto request) {
        Objects.requireNonNull(request, "request must not be null");

        return Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(statusOrDefault(request.getStatus()))
                .build();
    }

    public static void updateEntity(Task task, TaskRequestDto request) {
        Objects.requireNonNull(task, "task must not be null");
        Objects.requireNonNull(request, "request must not be null");

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(statusOrDefault(request.getStatus()));
    }

    public static TaskResponseDto toResponseDto(Task task) {
        Objects.requireNonNull(task, "task must not be null");

        return TaskResponseDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .ownerId(task.getOwner().getId())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .subtasks(task.getSubtasks()
                        .stream()
                        .map(SubtaskMapper::toResponseDto)
                        .toList())
                .build();
    }

    private static TaskStatus statusOrDefault(TaskStatus status) {
        return status == null ? TaskStatus.TODO : status;
    }
}
