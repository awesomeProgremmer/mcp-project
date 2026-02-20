package com.company.mcp.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    // Topic: when a user joins a channel
    @Bean
    public NewTopic userJoinedTopic() {
        return TopicBuilder.name("user-joined-channel")
                .partitions(1)
                .replicas(1)
                .build();
    }

    // Topic: when a user leaves a channel
    @Bean
    public NewTopic userLeftTopic() {
        return TopicBuilder.name("user-left-channel")
                .partitions(1)
                .replicas(1)
                .build();
    }

    // Topic: when a message is sent in a channel
    @Bean
    public NewTopic channelMessageTopic() {
        return TopicBuilder.name("channel-message")
                .partitions(1)
                .replicas(1)
                .build();
    }
}