package com.taskmanager.service;

import com.taskmanager.dto.SubtaskMapper;
import com.taskmanager.dto.SubtaskRequestDto;
import com.taskmanager.dto.SubtaskResponseDto;
import com.taskmanager.entity.Subtask;
import com.taskmanager.entity.Task;
import com.taskmanager.exception.SubtaskNotFoundException;
import com.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SubtaskService {

    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;

    public SubtaskService(
            TaskRepository taskRepository,
            CurrentUserService currentUserService) {
        this.taskRepository = taskRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public SubtaskResponseDto createSubtask(
            UUID taskId,
            SubtaskRequestDto request) {
        Task task = findTask(taskId);
        currentUserService.requireOwner(task);
        Subtask subtask = SubtaskMapper.toEntity(request);
        task.addSubtask(subtask);
        taskRepository.saveAndFlush(task);
        return SubtaskMapper.toResponseDto(subtask);
    }

    public List<SubtaskResponseDto> getSubtasks(UUID taskId) {
        Task task = findTask(taskId);
        currentUserService.requireOwnerOrAdmin(task);
        return task.getSubtasks()
                .stream()
                .map(SubtaskMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public SubtaskResponseDto toggleCompleted(UUID taskId, UUID subtaskId) {
        Task task = findTask(taskId);
        currentUserService.requireOwner(task);
        Subtask subtask = findSubtask(task, taskId, subtaskId);
        subtask.setCompleted(!subtask.isCompleted());
        taskRepository.saveAndFlush(task);
        return SubtaskMapper.toResponseDto(subtask);
    }

    @Transactional
    public void deleteSubtask(UUID taskId, UUID subtaskId) {
        Task task = findTask(taskId);
        currentUserService.requireOwner(task);
        Subtask subtask = findSubtask(task, taskId, subtaskId);
        task.removeSubtask(subtask);
        taskRepository.saveAndFlush(task);
    }

    private Task findTask(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    private Subtask findSubtask(Task task, UUID taskId, UUID subtaskId) {
        return task.getSubtasks()
                .stream()
                .filter(subtask -> subtaskId.equals(subtask.getId()))
                .findFirst()
                .orElseThrow(() ->
                        new SubtaskNotFoundException(taskId, subtaskId));
    }
}
