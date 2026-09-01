package com.epam.java.specialization.trainer_workload.consumer;

import com.epam.java.specialization.common.dto.TrainerWorkloadRequestDto;
import com.epam.java.specialization.trainer_workload.service.interfaces.TrainerWorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrainerWorkloadConsumer {

    private final TrainerWorkloadService trainerWorkloadService;

    @KafkaListener(
            topics = "${app.kafka.topics.trainer-workload:trainer-workload-topic}",
            groupId = "${spring.kafka.consumer.group-id:trainer-workload-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeWorkloadEvent(
            @Payload TrainerWorkloadRequestDto workloadRequest,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received workload event from partition {} with offset {}. Key: {}, Payload: {}",
                partition, offset, key, workloadRequest);

        try {
            trainerWorkloadService.processTrainingWorkload(workloadRequest);
            log.info("Successfully processed workload event for trainer: {}", workloadRequest.getUsername());
        } catch (Exception e) {
            log.error("Error processing workload event for trainer '{}' (offset: {}): {}",
                    workloadRequest.getUsername(), offset, e.getMessage(), e);
            
            throw e;
        }
    }
}