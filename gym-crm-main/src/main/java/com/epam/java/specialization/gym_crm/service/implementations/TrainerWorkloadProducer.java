package com.epam.java.specialization.gym_crm.service.implementations;

import com.epam.java.specialization.common.dto.TrainerWorkloadRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadProducer {

    private final JmsTemplate jmsTemplate;

    @Value("${app.jms.queue.workload:trainer-workload-queue}")
    private String destinationQueue;

    public void sendWorkloadRequest(TrainerWorkloadRequestDto workloadRequestDto) {
        log.info("Sending workload request for trainer: {} to queue: {}",
                workloadRequestDto.getUsername(), destinationQueue);
        try {
            jmsTemplate.convertAndSend(destinationQueue, workloadRequestDto);
            log.info("Successfully sent workload request for trainer: {}", workloadRequestDto.getUsername());
        } catch (Exception ex) {
            log.error("Failed to send workload message for trainer: {}. Error: {}",
                    workloadRequestDto.getUsername(), ex.getMessage(), ex);
            throw ex;
        }
    }
}