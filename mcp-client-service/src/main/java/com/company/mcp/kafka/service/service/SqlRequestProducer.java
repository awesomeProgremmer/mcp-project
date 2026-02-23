package com.company.mcp.kafka.service.service;

import com.company.mcp.kafka.service.dto.SqlRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class SqlRequestProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String TOPIC = "sql-requests";

    // stores pending requests waiting for Python response
    private final Map<String, CompletableFuture<String>> pendingRequests
            = new ConcurrentHashMap<>();

    public SqlRequestProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // ─── NEW: sends to Kafka and waits for response ───────────────
    public String sendAndWait(SqlRequest request) throws Exception {
        // generate unique ID
        String requestId = UUID.randomUUID().toString();
        request.setRequestId(requestId);

        // create future
        CompletableFuture<String> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);

        // send to Kafka
        String json = objectMapper.writeValueAsString(request);
        kafkaTemplate.send(TOPIC, requestId, json);
        System.out.println("Sent to Kafka: " + json);

        try {
            // wait max 30 seconds
            return future.get(30, TimeUnit.SECONDS);
        } finally {
            pendingRequests.remove(requestId);
        }
    }

    // ─── NEW: called by consumer when Python responds ─────────────
    public void completeRequest(String requestId, String sql) {
        if (requestId == null) {  // ← ADD null check
            System.out.println("⚠️ requestId is null — ignoring");
            return;
        }
        CompletableFuture<String> future = pendingRequests.get(requestId);
        if (future != null) {
            future.complete(sql);
            System.out.println("✅ Completed request: " + requestId);
        } else {
            System.out.println("⚠️ No pending request found for: " + requestId);
        }
    }

    // ─── OLD: keep for reference but no longer used ───────────────
    public void sendSqlRequest(SqlRequest request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            kafkaTemplate.send(TOPIC, request.getRequestId(), json);
            System.out.println("Sent to Kafka: " + json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}