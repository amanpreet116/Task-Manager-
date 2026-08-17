package com.taskmanager.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * A topic is a named stream of messages. Partitions split that stream into
 * ordered lanes so Kafka can process it in parallel. Consumers sharing a
 * consumer group divide those partitions between themselves, so each message
 * is handled by one member of that group.
 */
@Configuration
public class KafkaTopicConfig {

    public static final String TASK_EVENTS_TOPIC = "task-events";

    @Bean
    public NewTopic taskEventsTopic() {
        return TopicBuilder.name(TASK_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
