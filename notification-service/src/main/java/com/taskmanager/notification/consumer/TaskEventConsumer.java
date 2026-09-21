package com.taskmanager.notification.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.notification.dto.TaskEventDto;
import com.taskmanager.notification.exception.InvalidTaskEventException;
import com.taskmanager.notification.service.NotificationDeliveryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TaskEventConsumer {

    private static final Set<String> VALID_STATUSES =
            Set.of("TODO", "IN_PROGRESS", "DONE");

    private final ObjectMapper objectMapper;
    private final NotificationDeliveryService notificationDeliveryService;

    public TaskEventConsumer(
            ObjectMapper objectMapper,
            NotificationDeliveryService notificationDeliveryService) {
        this.objectMapper = objectMapper;
        this.notificationDeliveryService = notificationDeliveryService;
    }

    @KafkaListener(
            topics = "task-events",
            groupId = "notification-service-group"
    )
    public void consume(String payload) {
        TaskEventDto event = parseAndValidate(payload);
        String message = buildMessage(event);
        notificationDeliveryService.deliver(event, message);
    }

    private TaskEventDto parseAndValidate(String payload) {
        final TaskEventDto event;

        try {
            event = objectMapper.readValue(payload, TaskEventDto.class);
        } catch (JsonProcessingException exception) {
            throw new InvalidTaskEventException(
                    "Task event is not valid JSON",
                    exception
            );
        }

        if (event.eventType() == null
                || event.taskId() == null
                || event.title() == null
                || event.title().isBlank()
                || event.status() == null
                || !VALID_STATUSES.contains(event.status())
                || event.timestamp() == null) {
            throw new InvalidTaskEventException(
                    "Task event is missing required or valid fields");
        }

        return event;
    }

    private String buildMessage(TaskEventDto event) {
        return switch (event.eventType()) {
            case "TASK_CREATED" ->
                    "Task '" + event.title() + "' created";
            case "TASK_STATUS_CHANGED" ->
                    "Task '" + event.title() + "' status changed to "
                            + event.status();
            default -> throw new InvalidTaskEventException(
                    "Unsupported event type: " + event.eventType());
        };
    }
}
