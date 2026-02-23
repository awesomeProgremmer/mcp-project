package com.company.mcp.kafka.service.controller;

import com.company.mcp.kafka.service.dto.SqlRequest;
import com.company.mcp.kafka.service.service.SqlRequestProducer;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sql")
@CrossOrigin(origins = "http://localhost:4200")
public class SqlController {

    private final SqlRequestProducer producer;

    public SqlController(SqlRequestProducer producer) {
        this.producer = producer;
    }

    @PostMapping("/generate")
    public String sendRequest(@RequestBody SqlRequest request) {
        try {
            // ← changed: now waits for Python response
            String sql = producer.sendAndWait(request);
            System.out.println("📤 Sending to Angular: " + sql);
            return sql;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}