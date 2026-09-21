package com.taskmanager.notification.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.notification.config.KafkaConsumerConfig;
import com.taskmanager.notification.entity.Notification;
import com.taskmanager.notification.entity.NotificationStatus;
import com.taskmanager.notification.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class DeadLetterTaskEventConsumer {

    private static final int MAX_AUDIT_PAYLOAD_LENGTH = 2_000;

    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationRepository;

    public DeadLetterTaskEventConsumer(
            ObjectMapper objectMapper,
            NotificationRepository notificationRepository) {
        this.objectMapper = objectMapper;
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(
            topics = KafkaConsumerConfig.TASK_EVENTS_DLT,
            groupId = "notification-service-dlt-group"
    )
    public void consumeDeadLetter(String payload) {
        log.error("Poison task event routed to DLT: {}", payload);

        try {
            Notification failedNotification = Notification.builder()
                    .taskId(extractTaskId(payload))
                    .message("Failed to process task event: "
                            + truncate(payload))
                    .status(NotificationStatus.FAILED)
                    .build();
            notificationRepository.save(failedNotification);
        } catch (RuntimeException exception) {
            log.error(
                    "Could not persist FAILED notification audit record",
                    exception
            );
        }
    }

    private UUID extractTaskId(String payload) {
        try {
            JsonNode taskId = objectMapper.readTree(payload).path("taskId");
            return taskId.isTextual()
                    ? UUID.fromString(taskId.asText())
                    : null;
        } catch (Exception exception) {
            return null;
        }
    }

    private String truncate(String payload) {
        return payload.length() <= MAX_AUDIT_PAYLOAD_LENGTH
                ? payload
                : payload.substring(0, MAX_AUDIT_PAYLOAD_LENGTH);
    }
}
