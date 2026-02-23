package com.company.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class McpClientServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpClientServiceApplication.class, args);
	}

}
