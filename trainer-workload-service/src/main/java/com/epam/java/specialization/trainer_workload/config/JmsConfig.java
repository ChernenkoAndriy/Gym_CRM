package com.epam.java.specialization.trainer_workload.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJms
public class JmsConfig {

    @Value("${spring.jms.broker-url}")
    private String brokerUrl;

    @Value("${spring.jms.user:admin}")
    private String user;

    @Value("${spring.jms.password:admin}")
    private String password;

    @Value("${app.jms.concurrency:3-10}")
    private String concurrency;

    @Value("${app.jms.redelivery.max-deliveries:3}")
    private int maxRedeliveries;

    @Value("${app.jms.redelivery.initial-delay:1000}")
    private long initialRedeliveryDelay;

    @Value("${app.jms.redelivery.back-off-multiplier:2.0}")
    private double backOffMultiplier;

    @Bean
    public RedeliveryPolicy redeliveryPolicy() {
        RedeliveryPolicy policy = new RedeliveryPolicy();
        policy.setMaximumRedeliveries(maxRedeliveries);
        policy.setInitialRedeliveryDelay(initialRedeliveryDelay);
        policy.setBackOffMultiplier(backOffMultiplier);
        policy.setUseExponentialBackOff(true);
        return policy;
    }

    @Bean
    public ConnectionFactory connectionFactory(RedeliveryPolicy redeliveryPolicy) {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(brokerUrl);
        connectionFactory.setUserName(user);
        connectionFactory.setPassword(password);
        connectionFactory.setTrustAllPackages(true);
        connectionFactory.setRedeliveryPolicy(redeliveryPolicy);
        return connectionFactory;
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        converter.setObjectMapper(objectMapper);

        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put("TrainerWorkloadRequestDto",
                com.epam.java.specialization.common.dto.TrainerWorkloadRequestDto.class);
        converter.setTypeIdMappings(typeIdMappings);

        return converter;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrency(concurrency);
        factory.setSessionTransacted(true);
        factory.setPubSubDomain(false);
        return factory;
    }
}