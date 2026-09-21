package com.taskmanager.controller;

import com.taskmanager.dto.AttachmentResponseDto;
import com.taskmanager.service.AttachmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks/{taskId}/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponseDto> upload(
            @PathVariable UUID taskId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attachmentService.upload(taskId, file));
    }

    @GetMapping
    public List<AttachmentResponseDto> getAttachments(
            @PathVariable UUID taskId) {
        return attachmentService.getAttachments(taskId);
    }
}
