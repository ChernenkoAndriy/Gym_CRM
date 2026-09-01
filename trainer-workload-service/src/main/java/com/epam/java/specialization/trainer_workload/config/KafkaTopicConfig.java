package com.epam.java.specialization.trainer_workload.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.topics.trainer-workload:trainer-workload-topic}")
    private String workloadTopic;

    @Value("${app.kafka.topics.trainer-workload-dlt:trainer-workload-topic.DLT}")
    private String workloadDltTopic;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic workloadTopic() {
        return TopicBuilder.name(workloadTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic workloadDltTopic() {
        return TopicBuilder.name(workloadDltTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}