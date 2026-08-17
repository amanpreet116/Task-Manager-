package com.taskmanager.controller;

import com.taskmanager.dto.SubtaskRequestDto;
import com.taskmanager.dto.SubtaskResponseDto;
import com.taskmanager.service.SubtaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks/{taskId}/subtasks")
public class SubtaskController {

    private final SubtaskService subtaskService;

    public SubtaskController(SubtaskService subtaskService) {
        this.subtaskService = subtaskService;
    }

    @PostMapping
    public ResponseEntity<SubtaskResponseDto> createSubtask(
            @PathVariable UUID taskId,
            @Valid @RequestBody SubtaskRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subtaskService.createSubtask(taskId, request));
    }

    @GetMapping
    public List<SubtaskResponseDto> getSubtasks(@PathVariable UUID taskId) {
        return subtaskService.getSubtasks(taskId);
    }

    @PatchMapping("/{subtaskId}")
    public SubtaskResponseDto toggleCompleted(
            @PathVariable UUID taskId,
            @PathVariable UUID subtaskId) {
        return subtaskService.toggleCompleted(taskId, subtaskId);
    }

    @DeleteMapping("/{subtaskId}")
    public ResponseEntity<Void> deleteSubtask(
            @PathVariable UUID taskId,
            @PathVariable UUID subtaskId) {
        subtaskService.deleteSubtask(taskId, subtaskId);
        return ResponseEntity.noContent().build();
    }
}
