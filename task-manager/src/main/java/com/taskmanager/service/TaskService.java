package com.taskmanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.dto.TaskMapper;
import com.taskmanager.dto.TaskRequestDto;
import com.taskmanager.dto.TaskResponseDto;
import com.taskmanager.entity.OutboxEvent;
import com.taskmanager.entity.OutboxStatus;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.events.TaskEventDto;
import com.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.repository.OutboxEventRepository;
import com.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;
    private final S3StorageService s3StorageService;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public TaskService(
            TaskRepository taskRepository,
            CurrentUserService currentUserService,
            S3StorageService s3StorageService,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {
        this.taskRepository = taskRepository;
        this.currentUserService = currentUserService;
        this.s3StorageService = s3StorageService;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public TaskResponseDto createTask(TaskRequestDto request) {
        Task task = TaskMapper.toEntity(request);
        task.setOwner(currentUserService.getCurrentUser());
        Task savedTask = taskRepository.saveAndFlush(task);
        enqueueTaskEvent("TASK_CREATED", savedTask);

        return TaskMapper.toResponseDto(savedTask);
    }

    public TaskResponseDto getTaskById(UUID id) {
        Task task = findTask(id);
        currentUserService.requireOwnerOrAdmin(task);
        return TaskMapper.toResponseDto(task);
    }

    public List<TaskResponseDto> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(TaskMapper::toResponseDto)
                .toList();
    }

    public List<TaskResponseDto> getMyTasks() {
        return taskRepository.findAllByOwner_IdOrderByCreatedAtDesc(
                        currentUserService.getCurrentUser().getId())
                .stream()
                .map(TaskMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public TaskResponseDto updateTask(UUID id, TaskRequestDto request) {
        Task task = findTask(id);
        currentUserService.requireOwner(task);
        TaskStatus previousStatus = task.getStatus();
        TaskMapper.updateEntity(task, request);
        Task savedTask = taskRepository.saveAndFlush(task);

        if (previousStatus != savedTask.getStatus()) {
            enqueueTaskEvent("TASK_STATUS_CHANGED", savedTask);
        }

        return TaskMapper.toResponseDto(savedTask);
    }

    @Transactional
    public void deleteTask(UUID id) {
        Task task = findTask(id);
        task.getAttachments()
                .forEach(attachment ->
                        s3StorageService.delete(attachment.getS3Key()));
        taskRepository.delete(task);
    }

    private Task findTask(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    private void enqueueTaskEvent(String eventType, Task task) {
        TaskEventDto event = new TaskEventDto(
                eventType,
                task.getId(),
                task.getTitle(),
                task.getStatus().name(),
                Instant.now()
        );

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventType(eventType)
                .payload(serializeTaskEvent(event))
                .status(OutboxStatus.PENDING)
                .nextAttemptAt(LocalDateTime.now())
                .build();
        outboxEventRepository.save(outboxEvent);
    }

    private String serializeTaskEvent(TaskEventDto event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Could not serialize task event",
                    exception
            );
        }
    }
}
