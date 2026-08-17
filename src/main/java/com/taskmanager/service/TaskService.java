package com.taskmanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.dto.TaskMapper;
import com.taskmanager.dto.TaskRequestDto;
import com.taskmanager.dto.TaskResponseDto;
import com.taskmanager.entity.OutboxEvent;
import com.taskmanager.entity.OutboxStatus;
import com.taskmanager.entity.Task;
import com.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.repository.OutboxEventRepository;
import com.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

        OutboxEvent event = OutboxEvent.builder()
                .eventType("TASK_CREATED")
                .payload(createTaskCreatedPayload(savedTask))
                .status(OutboxStatus.PENDING)
                .nextAttemptAt(LocalDateTime.now())
                .build();
        outboxEventRepository.save(event);

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

    @Transactional
    public TaskResponseDto updateTask(UUID id, TaskRequestDto request) {
        Task task = findTask(id);
        currentUserService.requireOwner(task);
        TaskMapper.updateEntity(task, request);
        return TaskMapper.toResponseDto(taskRepository.saveAndFlush(task));
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

    private String createTaskCreatedPayload(Task task) {
        Map<String, Object> payload = Map.of(
                "eventType", "TASK_CREATED",
                "taskId", task.getId(),
                "title", task.getTitle(),
                "status", task.getStatus(),
                "ownerId", task.getOwner().getId(),
                "createdAt", task.getCreatedAt()
        );

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Could not serialize TASK_CREATED event",
                    exception
            );
        }
    }
}
