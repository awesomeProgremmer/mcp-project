package com.company.mcp.kafka.service.controller;

import com.company.mcp.kafka.service.dto.SqlRequest;
import com.company.mcp.kafka.service.service.SqlRequestProducer;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sql")
public class SqlController {

    private final SqlRequestProducer producer;

    public SqlController(SqlRequestProducer producer) {
        this.producer = producer;
    }

    @PostMapping("/generate")
    public String sendRequest(@RequestBody SqlRequest request) {
        producer.sendSqlRequest(request);
        return "SQL request sent to Kafka";
    }
}