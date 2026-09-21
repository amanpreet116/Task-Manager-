package com.taskmanager.notification.service;

import com.taskmanager.notification.dto.NotificationResponseDto;
import com.taskmanager.notification.entity.Notification;
import com.taskmanager.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;

    public NotificationQueryService(
            NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Page<NotificationResponseDto> getRecentNotifications(
            Pageable pageable) {
        return notificationRepository
                .findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponseDto);
    }

    private NotificationResponseDto toResponseDto(
            Notification notification) {
        return new NotificationResponseDto(
                notification.getId(),
                notification.getTaskId(),
                notification.getMessage(),
                notification.getStatus(),
                notification.getCreatedAt()
        );
    }
}
