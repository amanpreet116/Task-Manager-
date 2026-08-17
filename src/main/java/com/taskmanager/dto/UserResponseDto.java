package com.taskmanager.dto;

import com.taskmanager.entity.Role;

import java.util.UUID;

public record UserResponseDto(UUID id, String email, Role role) {
}
