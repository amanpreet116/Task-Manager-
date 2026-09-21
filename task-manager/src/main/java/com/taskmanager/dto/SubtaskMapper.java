package com.taskmanager.dto;

import com.taskmanager.entity.Subtask;

import java.util.Objects;

public final class SubtaskMapper {

    private SubtaskMapper() {
    }

    public static Subtask toEntity(SubtaskRequestDto request) {
        Objects.requireNonNull(request, "request must not be null");

        return Subtask.builder()
                .title(request.getTitle())
                .build();
    }

    public static SubtaskResponseDto toResponseDto(Subtask subtask) {
        Objects.requireNonNull(subtask, "subtask must not be null");

        return SubtaskResponseDto.builder()
                .id(subtask.getId())
                .title(subtask.getTitle())
                .completed(subtask.isCompleted())
                .build();
    }
}
