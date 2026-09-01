package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.common.dto.ActionType;
import com.epam.java.specialization.common.dto.TrainerWorkloadRequestDto;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadProducerTest {

    @Mock
    private KafkaTemplate<String, TrainerWorkloadRequestDto> kafkaTemplate;

    @InjectMocks
    private TrainerWorkloadProducer producer;

    private static final String TOPIC_NAME = "trainer-workload-topic-test";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(producer, "trainerWorkloadTopic", TOPIC_NAME);
    }

    @Test
    @DisplayName("Should send workload request DTO to configured Kafka topic with trainer key")
    void sendWorkloadRequest_ShouldSendToTopic() {
        Date testDate = new Date();
        TrainerWorkloadRequestDto requestDto = TrainerWorkloadRequestDto.builder()
                .username("Trainer.John")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .trainingDate(testDate)
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();

        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition(TOPIC_NAME, 0), 0, 0, 0L, 0, 0);
        SendResult<String, TrainerWorkloadRequestDto> sendResult = new SendResult<>(null, recordMetadata);
        CompletableFuture<SendResult<String, TrainerWorkloadRequestDto>> future = CompletableFuture.completedFuture(sendResult);

        when(kafkaTemplate.send(eq(TOPIC_NAME), eq("Trainer.John"), eq(requestDto))).thenReturn(future);

        producer.sendWorkloadRequest(requestDto);

        verify(kafkaTemplate, times(1)).send(eq(TOPIC_NAME), eq("Trainer.John"), eq(requestDto));
    }
}