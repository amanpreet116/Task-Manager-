package com.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class SubtaskResponseDto {

    private final UUID id;
    private final String title;
    private final boolean completed;
}
