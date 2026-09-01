package com.epam.java.specialization.trainer_workload.consumer;

import com.epam.java.specialization.common.dto.TrainerWorkloadRequestDto;
import com.epam.java.specialization.trainer_workload.service.interfaces.TrainerWorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadConsumer {

    private final TrainerWorkloadService workloadService;

    @JmsListener(
            destination = "${app.jms.queue.workload:trainer-workload-queue}",
            containerFactory = "jmsListenerContainerFactory"
    )
    public void receiveWorkloadMessage(TrainerWorkloadRequestDto requestDto) {
        log.info("Received workload request message for trainer: {} with action: {}",
                requestDto.getUsername(), requestDto.getActionType());

        try {
            workloadService.processTrainingWorkload(requestDto);
            log.info("Successfully processed workload update for trainer: {}", requestDto.getUsername());
        } catch (Exception ex) {
            log.error("Error processing workload update for trainer: {}. Message will be retried according to RedeliveryPolicy. Error: {}",
                    requestDto.getUsername(), ex.getMessage(), ex);
            // Прокидаємо виняток далі, щоб транзакція JMS відкотилася і спрацював механізм Redelivery/DLQ
            throw ex;
        }
    }
}