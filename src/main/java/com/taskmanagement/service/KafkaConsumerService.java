package com.taskmanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    @KafkaListener(topics = "task-events", groupId = "task-management-group")
    public void consumeTaskEvent(String message) {
        log.info("Consumed task event: {}", message);
        // Process the event (e.g., update caches, trigger notifications, etc.)
    }

    @KafkaListener(topics = "notification-events", groupId = "task-management-group")
    public void consumeNotificationEvent(String message) {
        log.info("Consumed notification event: {}", message);
        // Process notification events
    }
}
