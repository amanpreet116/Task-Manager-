package com.taskmanager.service;

import com.taskmanager.entity.OutboxEvent;
import com.taskmanager.entity.OutboxStatus;
import com.taskmanager.repository.OutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * {@code fixedDelay} waits 30 seconds after one run finishes before starting
 * the next. A cron expression instead runs at matching wall-clock times, such
 * as every minute on the minute, regardless of when the previous run began.
 */
@Slf4j
@Service
public class OutboxProcessor {

    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRIES = 5;
    private static final long INITIAL_BACKOFF_SECONDS = 30;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaEventPublisher kafkaEventPublisher;

    public OutboxProcessor(
            OutboxEventRepository outboxEventRepository,
            KafkaEventPublisher kafkaEventPublisher) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaEventPublisher = kafkaEventPublisher;
    }

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void processPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository.findReadyEvents(
                OutboxStatus.PENDING,
                LocalDateTime.now(),
                PageRequest.of(0, BATCH_SIZE)
        );

        events.forEach(this::processEvent);
    }

    private void processEvent(OutboxEvent event) {
        try {
            kafkaEventPublisher.publish(event);
            event.setStatus(OutboxStatus.SENT);
        } catch (RuntimeException exception) {
            int retryCount = event.getRetryCount() + 1;
            event.setRetryCount(retryCount);

            if (retryCount >= MAX_RETRIES) {
                event.setStatus(OutboxStatus.FAILED);
                log.error(
                        "Outbox event {} failed permanently after {} attempts",
                        event.getId(),
                        retryCount,
                        exception
                );
                return;
            }

            long backoffSeconds = INITIAL_BACKOFF_SECONDS
                    * (1L << (retryCount - 1));
            event.setNextAttemptAt(
                    LocalDateTime.now().plusSeconds(backoffSeconds));
            log.warn(
                    "Outbox event {} failed; retry {} scheduled in {} seconds",
                    event.getId(),
                    retryCount,
                    backoffSeconds,
                    exception
            );
        }
    }
}
