package com.taskmanager.service;

import com.taskmanager.config.KafkaTopicConfig;
import com.taskmanager.entity.OutboxEvent;
import com.taskmanager.exception.KafkaPublishException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class KafkaEventPublisher {

    private static final long PUBLISH_TIMEOUT_SECONDS = 10;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<Void> publish(OutboxEvent event) {
        CompletableFuture<Void> confirmation = new CompletableFuture<>();

        try {
            kafkaTemplate.send(
                    KafkaTopicConfig.TASK_EVENTS_TOPIC,
                    event.getId().toString(),
                    event.getPayload()
            ).whenComplete((result, exception) -> {
                if (exception != null) {
                    confirmation.completeExceptionally(
                            new KafkaPublishException(
                                    "Kafka did not acknowledge the outbox event",
                                    exception
                            )
                    );
                    return;
                }
                confirmation.complete(null);
            });
        } catch (RuntimeException exception) {
            confirmation.completeExceptionally(
                    new KafkaPublishException(
                            "Kafka publish could not be started",
                            exception
                    )
            );
        }

        return confirmation.orTimeout(
                PUBLISH_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
        );
    }
}
