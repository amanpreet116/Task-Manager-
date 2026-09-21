package com.taskmanager.notification.service;

import com.taskmanager.notification.dto.TaskEventDto;
import com.taskmanager.notification.entity.Notification;
import com.taskmanager.notification.entity.NotificationStatus;
import com.taskmanager.notification.exception.TransientNotificationException;
import com.taskmanager.notification.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class NotificationDeliveryService {

    private final NotificationRepository notificationRepository;

    public NotificationDeliveryService(
            NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Retryable(
            retryFor = {
                    TransientNotificationException.class,
                    TransientDataAccessException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1_000, multiplier = 2)
    )
    @Transactional
    public void deliver(TaskEventDto event, String message) {
        /*
         * A real email/SMS provider call belongs here. It should translate
         * temporary provider errors into TransientNotificationException.
         */
        Notification notification = Notification.builder()
                .taskId(event.taskId())
                .message(message)
                .status(NotificationStatus.DELIVERED)
                .build();
        notificationRepository.saveAndFlush(notification);

        log.info("Notification delivered: {}", message);
    }
}
