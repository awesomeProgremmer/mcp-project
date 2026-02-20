package com.company.mcp.kafka.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    // ─── Topic names ──────────────────────────────────────────────────
    public static final String TOPIC_USER_JOINED   = "user-joined-channel";
    public static final String TOPIC_USER_LEFT     = "user-left-channel";
    public static final String TOPIC_USER_CREATED  = "user-created";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // ─── User joined a channel ────────────────────────────────────────
    public void sendUserJoinedChannel(Long userId, Long channelId) {
        String message = String.format(
                "{\"event\":\"USER_JOINED\", \"userId\":%d, \"channelId\":%d}",
                userId, channelId
        );
        sendMessage(TOPIC_USER_JOINED, message);
    }

    // ─── User left a channel ──────────────────────────────────────────
    public void sendUserLeftChannel(Long userId, Long channelId) {
        String message = String.format(
                "{\"event\":\"USER_LEFT\", \"userId\":%d, \"channelId\":%d}",
                userId, channelId
        );
        sendMessage(TOPIC_USER_LEFT, message);
    }

    // ─── New user created ─────────────────────────────────────────────
    public void sendUserCreated(Long userId, String username) {
        String message = String.format(
                "{\"event\":\"USER_CREATED\", \"userId\":%d, \"username\":\"%s\"}",
                userId, username
        );
        sendMessage(TOPIC_USER_CREATED, message);
    }

    // ─── Generic send ─────────────────────────────────────────────────
    private void sendMessage(String topic, String message) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ Sent to topic [{}] → offset [{}] message: {}",
                        topic,
                        result.getRecordMetadata().offset(),
                        message);
            } else {
                log.error("❌ Failed to send to topic [{}]: {}", topic, ex.getMessage());
            }
        });
    }
}