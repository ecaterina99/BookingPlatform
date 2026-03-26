package com.server.booking.infrastructure.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class KafkaTopicConfig {

    @Bean
    public NewTopic bookingCreatedTopic() {
        return new NewTopic(
                "booking.created",
                1,
                (short) 1
        );
    }
}