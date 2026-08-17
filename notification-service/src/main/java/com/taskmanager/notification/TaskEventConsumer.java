package com.taskmanager.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TaskEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(TaskEventConsumer.class);

    private final ObjectMapper objectMapper;

    public TaskEventConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "task-events")
    public void consume(String payload) {
        try {
            JsonNode event = objectMapper.readTree(payload);
            log.info(
                    "Notification [{}]: task '{}' (id={}) was created with "
                            + "status {} for owner {}",
                    event.path("eventType").asText(),
                    event.path("title").asText(),
                    event.path("taskId").asText(),
                    event.path("status").asText(),
                    event.path("ownerId").asText()
            );
        } catch (JsonProcessingException exception) {
            log.error("Could not parse task event payload: {}", payload, exception);
        }
    }
}
