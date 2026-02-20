package com.company.mcp.kafka.service.service;

import com.company.mcp.kafka.service.dto.SqlRequest;
import com.fasterxml.jackson.databind.ObjectMapper;   // ✅ correct import
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class SqlRequestProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String TOPIC = "sql-requests";

    public SqlRequestProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

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