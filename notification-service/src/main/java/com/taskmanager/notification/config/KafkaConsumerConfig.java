package com.taskmanager.notification.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
public class KafkaConsumerConfig {

    public static final String TASK_EVENTS_DLT = "task-events-dlt";

    @Bean
    public NewTopic taskEventsDltTopic() {
        return TopicBuilder.name(TASK_EVENTS_DLT)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, String> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) -> new TopicPartition(
                                TASK_EVENTS_DLT,
                                record.partition()
                        )
                );
        recoverer.setFailIfSendResultIsError(true);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(1_000L, 2L)
        );
        errorHandler.setCommitRecovered(true);
        errorHandler.setRetryListeners(
                (record, exception, deliveryAttempt) -> log.warn(
                        "Task event failed: topic={}, partition={}, "
                                + "offset={}, attempt={}",
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        deliveryAttempt,
                        exception
                )
        );
        return errorHandler;
    }
}
