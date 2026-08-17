package com.taskmanager.repository;

import com.taskmanager.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttachmentRepository
        extends JpaRepository<Attachment, UUID> {

    List<Attachment> findAllByTask_IdOrderByUploadedAtAsc(UUID taskId);
}
