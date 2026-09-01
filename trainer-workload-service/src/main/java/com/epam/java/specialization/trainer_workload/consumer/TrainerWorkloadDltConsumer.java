package com.epam.java.specialization.trainer_workload.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrainerWorkloadDltConsumer {

    @KafkaListener(
            topics = "${app.kafka.topics.trainer-workload-dlt:trainer-workload-topic.DLT}",
            groupId = "${spring.kafka.consumer.group-id:trainer-workload-group}-dlt"
    )
    public void consumeDeadLetterMessage(
            @Payload Object failedPayload,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            @Header(value = KafkaHeaders.ORIGINAL_TOPIC, required = false) String originalTopic,
            @Header(value = KafkaHeaders.ORIGINAL_PARTITION, required = false) Integer originalPartition,
            @Header(value = KafkaHeaders.ORIGINAL_OFFSET, required = false) Long originalOffset,
            @Header(value = KafkaHeaders.EXCEPTION_MESSAGE, required = false) String exceptionMessage) {

        log.error("CRITICAL: Message routed to DLT! Key: '{}', Original Topic: '{}', " +
                        "Original Partition: '{}', Original Offset: '{}', Error: '{}', Payload: {}",
                key, originalTopic, originalPartition, originalOffset, exceptionMessage, failedPayload);

    }
}