package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.common.dto.TrainerWorkloadRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerWorkloadProducer {

    private final KafkaTemplate<String, TrainerWorkloadRequestDto> kafkaTemplate;

    @Value("${app.kafka.topics.trainer-workload:trainer-workload-topic}")
    private String trainerWorkloadTopic;

    public void sendWorkloadRequest(TrainerWorkloadRequestDto requestDto) {
        String key = requestDto.getTrainerUsername() != null ? requestDto.getTrainerUsername() : requestDto.getUsername();

        log.info("Sending workload event to Kafka topic '{}' with key '{}': {}",
                trainerWorkloadTopic, key, requestDto);

        CompletableFuture<SendResult<String, TrainerWorkloadRequestDto>> future =
                kafkaTemplate.send(trainerWorkloadTopic, key, requestDto);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Workload event delivered to '{}' [partition: {}, offset: {}]",
                        trainerWorkloadTopic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to deliver workload event to topic '{}': {}",
                        trainerWorkloadTopic, ex.getMessage(), ex);
            }
        });
    }
}