package com.company.mcp.kafka.service.service;



import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SqlResponseConsumer {

    private final SqlRequestProducer producer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SqlResponseConsumer(SqlRequestProducer producer) {
        this.producer = producer;
    }

    @KafkaListener(
            topics = "sql-responses",
            groupId = "spring-sql-response-group-v2"  // ← match new group
    )
    public void consume(String message) {
        try {
            Map<String, Object> response = objectMapper.readValue(message, Map.class);
            String requestId = (String) response.get("requestId");
            String sql = (String) response.get("sql");
            System.out.println("📩 RequestId: " + requestId); // ← ADD
            System.out.println("📩 SQL: " + sql);             // ← ADD
            System.out.println("Received SQL response for: " + requestId);
            System.out.println("SQL: " + sql);

            // complete the waiting future in producer
            producer.completeRequest(requestId, sql);

        } catch (Exception e) {
            System.out.println(" Error consuming response: " + e.getMessage());
            e.printStackTrace();
        }
    }
}