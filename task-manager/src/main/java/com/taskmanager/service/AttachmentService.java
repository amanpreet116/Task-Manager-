package com.taskmanager.service;

import com.taskmanager.dto.AttachmentResponseDto;
import com.taskmanager.entity.Attachment;
import com.taskmanager.entity.Task;
import com.taskmanager.exception.InvalidAttachmentException;
import com.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.repository.AttachmentRepository;
import com.taskmanager.repository.TaskRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;
    private final S3StorageService s3StorageService;

    public AttachmentService(
            AttachmentRepository attachmentRepository,
            TaskRepository taskRepository,
            CurrentUserService currentUserService,
            S3StorageService s3StorageService) {
        this.attachmentRepository = attachmentRepository;
        this.taskRepository = taskRepository;
        this.currentUserService = currentUserService;
        this.s3StorageService = s3StorageService;
    }

    @Transactional
    public AttachmentResponseDto upload(
            UUID taskId,
            MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidAttachmentException(
                    "The uploaded file must not be empty");
        }

        Task task = findTask(taskId);
        currentUserService.requireOwner(task);

        String key = "tasks/" + taskId + "/attachments/" + UUID.randomUUID();
        String fileName = cleanFileName(file.getOriginalFilename());
        String contentType = file.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : file.getContentType();

        s3StorageService.upload(key, file);

        try {
            Attachment attachment = Attachment.builder()
                    .fileName(fileName)
                    .s3Key(key)
                    .contentType(contentType)
                    .task(task)
                    .build();
            Attachment savedAttachment =
                    attachmentRepository.saveAndFlush(attachment);
            return toResponseDto(savedAttachment);
        } catch (RuntimeException exception) {
            try {
                s3StorageService.delete(key);
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            throw exception;
        }
    }

    public List<AttachmentResponseDto> getAttachments(UUID taskId) {
        Task task = findTask(taskId);
        currentUserService.requireOwnerOrAdmin(task);

        return attachmentRepository
                .findAllByTask_IdOrderByUploadedAtAsc(taskId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    private AttachmentResponseDto toResponseDto(Attachment attachment) {
        return new AttachmentResponseDto(
                attachment.getId(),
                attachment.getFileName(),
                attachment.getContentType(),
                attachment.getTaskId(),
                attachment.getUploadedAt(),
                s3StorageService.createDownloadUrl(attachment.getS3Key())
        );
    }

    private Task findTask(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    private String cleanFileName(String originalFileName) {
        String fileName = StringUtils.cleanPath(
                originalFileName == null ? "" : originalFileName);
        return fileName.isBlank() ? "attachment" : fileName;
    }
}
