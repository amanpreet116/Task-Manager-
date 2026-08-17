package com.taskmanager.service;

import com.taskmanager.config.KafkaTopicConfig;
import com.taskmanager.entity.OutboxEvent;
import com.taskmanager.exception.KafkaPublishException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class KafkaEventPublisher {

    private static final long PUBLISH_TIMEOUT_SECONDS = 10;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(OutboxEvent event) {
        try {
            kafkaTemplate.send(
                            KafkaTopicConfig.TASK_EVENTS_TOPIC,
                            event.getId().toString(),
                            event.getPayload()
                    )
                    .get(PUBLISH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new KafkaPublishException(
                    "Kafka publish was interrupted",
                    exception
            );
        } catch (ExecutionException | TimeoutException exception) {
            throw new KafkaPublishException(
                    "Kafka did not acknowledge the outbox event",
                    exception
            );
        }
    }
}
