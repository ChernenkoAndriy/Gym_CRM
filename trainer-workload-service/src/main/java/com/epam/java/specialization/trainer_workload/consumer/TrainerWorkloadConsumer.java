package com.epam.java.specialization.trainer_workload.consumer;

import com.epam.java.specialization.common.dto.TrainerWorkloadRequestDto;
import com.epam.java.specialization.trainer_workload.service.interfaces.TrainerWorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrainerWorkloadConsumer {

    private static final String TRANSACTION_ID_KEY = "transactionId";
    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";

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
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = TRANSACTION_ID_HEADER, required = false) Object transactionIdHeader) {

        String transactionId = extractTransactionId(transactionIdHeader);
        MDC.put(TRANSACTION_ID_KEY, transactionId);

        long startTime = System.currentTimeMillis();
        log.info("Started transaction [{}] for Kafka event. Partition: {}, Offset: {}, Key: {}",
                transactionId, partition, offset, key);

        try {
            trainerWorkloadService.processTrainingWorkload(workloadRequest);
            long duration = System.currentTimeMillis() - startTime;
            log.info("Finished transaction [{}] successfully. Trainer: {} (Duration: {}ms)",
                    transactionId, workloadRequest != null ? workloadRequest.getUsername() : "UNKNOWN", duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Failed transaction [{}] for trainer '{}' (offset: {}): {} (Duration: {}ms)",
                    transactionId, workloadRequest != null ? workloadRequest.getUsername() : "UNKNOWN",
                    offset, e.getMessage(), duration, e);
            throw e;
        } finally {
            MDC.remove(TRANSACTION_ID_KEY);
        }
    }

    private String extractTransactionId(Object header) {
        if (header instanceof String str && !str.trim().isEmpty()) {
            return str;
        }
        if (header instanceof byte[] bytes && bytes.length > 0) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return UUID.randomUUID().toString();
    }
}